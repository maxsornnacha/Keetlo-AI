package com.keetlo.ai.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

import com.keetlo.ai.service.CustomOAuth2UserService;
import com.keetlo.ai.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class SecurityConfig {
    private final CustomOAuth2UserService customOAuth2UserService;
    @Value("${oauth2.redirect.url}")
    private String OAUTH2_REDIRECT_URL;
    private JwtUtil jwtUtil;
    private final ClientRegistrationRepository clientRegistrationRepository;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService, JwtUtil jwtUtil,
            ClientRegistrationRepository clientRegistrationRepository) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.jwtUtil = jwtUtil;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        DefaultOAuth2AuthorizationRequestResolver defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, "/oauth2/authorization");

        // 👇 Add prompt=consent here
        OAuth2AuthorizationRequestResolver customResolver = new OAuth2AuthorizationRequestResolver() {
            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                return customize(defaultResolver.resolve(request));
            }

            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
                return customize(defaultResolver.resolve(request, clientRegistrationId));
            }

            private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest req) {
                if (req == null)
                    return null;

                Map<String, Object> params = new HashMap<>(req.getAdditionalParameters());
                params.put("prompt", "consent");

                return OAuth2AuthorizationRequest.from(req)
                        .additionalParameters(params)
                        .build();
            }
        };

        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(a -> a.authorizationRequestResolver(customResolver))
                        .userInfoEndpoint(infoEndpoint -> infoEndpoint.userService(customOAuth2UserService))
                        .successHandler((_, response, authentication) -> {
                            OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
                            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
                            String registrationId = oauthToken.getAuthorizedClientRegistrationId();
                            Map<String, Object> attributes = oauthUser.getAttributes();
                            String oauthId = registrationId.equals("google") ? (String) attributes.get("sub")
                                    : String.valueOf(attributes.get("id"));
                            String oauthtoken = jwtUtil.generateToken(oauthId);

                            response.sendRedirect(OAUTH2_REDIRECT_URL + "?oauthToken=" + oauthtoken + "&status=success"
                                    + "&registrationId=" + registrationId);
                        })
                        .failureHandler((_, response, exception) -> {
                            String errorMessage = exception.getMessage();
                            response.sendRedirect(OAUTH2_REDIRECT_URL + "?status=error&message="
                                    + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
                        }));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}