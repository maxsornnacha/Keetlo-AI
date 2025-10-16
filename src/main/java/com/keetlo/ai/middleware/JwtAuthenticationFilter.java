package com.keetlo.ai.middleware;

import com.keetlo.ai.service.SessionService;
import com.keetlo.ai.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.cors.CorsUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${client.url.csv}")            // CSV: http://localhost:3015,https://www.keetlo.com,https://keetlo.com
    private String clientUrlCsv;

    @Autowired private JwtUtil jwtUtil;
    @Autowired private SessionService sessionService;

    // ---- CORS helpers ------------------------------------------------------

    private List<String> allowedOrigins() {
        return Arrays.stream(clientUrlCsv.split("\\s*,\\s*"))
                     .filter(s -> !s.isBlank())
                     .toList();
    }

    private boolean isAllowedOrigin(String origin) {
        if (origin == null) return false;
        for (String allowed : allowedOrigins()) {
            // exact match
            if (origin.equalsIgnoreCase(allowed)) return true;
            // simple pattern for subdomains: https://*.keetlo.com
            if (allowed.equalsIgnoreCase("https://*.keetlo.com")
                && origin.toLowerCase().endsWith(".keetlo.com")
                && origin.toLowerCase().startsWith("https://")) return true;
            // dev wildcard: http://localhost:*
            if (allowed.equalsIgnoreCase("http://localhost:*")
                && origin.toLowerCase().startsWith("http://localhost")) return true;
        }
        return false;
    }

    private void applyCorsHeaders(HttpServletRequest req, HttpServletResponse res) {
        String origin = req.getHeader("Origin");
        if (isAllowedOrigin(origin)) {
            res.setHeader("Access-Control-Allow-Origin", origin);         // must echo the request origin (no wildcard with credentials)
            res.setHeader("Vary", "Origin");                              // proxy/cdn correctness
            res.setHeader("Access-Control-Allow-Credentials", "true");
            res.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            res.setHeader("Access-Control-Allow-Headers", "Authorization,Content-Type");
            res.setHeader("Access-Control-Max-Age", "3600");
        }
    }

    // -----------------------------------------------------------------------

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        // Always set CORS headers first, so even errors include them
        applyCorsHeaders(request, response);
        // Handle CORS preflight here (and stop)
        if (CorsUtils.isPreFlightRequest(request)) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String auth = request.getHeader("Authorization");

        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                String userId = jwtUtil.validateTokenAndGetValue(token);
                String sessionUserId = sessionService.getUserIdByToken(token);

                if (sessionUserId != null && sessionUserId.equals(userId)) {
                    var authentication = new UsernamePasswordAuthenticationToken(
                        userId, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.getWriter().write("{\"message\":\"Token expired or invalid\"}");
                    return;
                }
            } catch (Exception e) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write("{\"message\":\"Token invalid\"}");
                return;
            }
        } else {
            // No/Bad Authorization header → short-circuit 401 but with CORS headers already set
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("{\"message\":\"Authorization header missing or invalid\"}");
            return;
        }

        chain.doFilter(request, response);
    }


    @Override
    protected boolean shouldNotFilter(@SuppressWarnings("null") HttpServletRequest request) throws ServletException {
        
       //if (CorsUtils.isPreFlightRequest(request)) return true;

        String path = request.getRequestURI();
        // Exclude public endpoints
        String[] excludedPaths = { "/auth", "/stripe/webhook", "/images", "/subscription", "/ws", "/public", "/contact-message" };
        String[] includePaths = {"/auth/logout"};
           for (String p : includePaths) {
            if (path.startsWith(p) ) {
                return false;
            }
        }

        for (String p : excludedPaths) {
            if (path.startsWith(p)) {
                return true;
            }
        }
        return false;
    }
}
