package com.keetlo.ai.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.keetlo.ai.model.Page;
import com.keetlo.ai.model.PublicProject;
import com.keetlo.ai.service.PublicProjectService;
import com.keetlo.ai.service.SessionService;
import com.keetlo.ai.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/public/projects")
public class PublicProjectController {

        private final PublicProjectService publicProjectService;
        private final SessionService sessionService;
        private final JwtUtil jwtUtil;

        public PublicProjectController(PublicProjectService publicProjectService, SessionService sessionService, JwtUtil jwtUtil) {
                this.publicProjectService = publicProjectService;
                this.sessionService = sessionService;
                this.jwtUtil = jwtUtil;
        }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getPublicProjects(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "tags", required = false) String tagsCsv,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "16") int pageSize,
            HttpServletRequest request
    ) {
        String token = request.getHeader("Authorization");
        if(token != null && token.startsWith("Bearer ")){
         token = token.substring(7);
        }
        String userId = null;
        if(token != null && sessionService.getUserIdByToken(token).equals(jwtUtil.validateTokenAndGetValue(token))){
            String resultUserId = sessionService.getUserIdByToken(token);
            userId = resultUserId;
        }

        Map<String, Object> response = new HashMap<>();
        try{

        Map<String, Object> result = publicProjectService.getPublicProjects(
                q, sort, type, tagsCsv, page, pageSize, userId
        );
        return ResponseEntity.ok(result);
        } catch (Exception e) {
        e.printStackTrace();
        response.put("projects", Collections.emptyList());
        response.put("totalPages", 0);
        response.put("currentPage", page);
        return ResponseEntity.status(500).body(response);
    }
    }

    @GetMapping("/tags")
    public ResponseEntity<List<String>> getAllPublicTags() {
        return ResponseEntity.ok(publicProjectService.getAllPublicTags());
    }

     @GetMapping("/types")
    public ResponseEntity<List<String>> getAllPublicTypes() {
        return ResponseEntity.ok(publicProjectService.getAllPublicTypes());
    }

    @GetMapping("/{projectId}")
        public ResponseEntity<?> getProject(@PathVariable String projectId) {
                PublicProject project = publicProjectService.getProjectByProjectId(projectId);
                if (project == null) {
                        return ResponseEntity.status(404).body("The project not found");
                }
                return ResponseEntity.ok(project);
    }

    @GetMapping("/main-html/{projectId}")
    public ResponseEntity<?> getMainHtml(@PathVariable String projectId) {
                String mainHtml = publicProjectService.getProjectMainHtmlContentByProjectId(projectId);
                if (mainHtml == null) {
                        return ResponseEntity.status(404).body("The project not found");
                }
                return ResponseEntity.ok(mainHtml);
    }

     @GetMapping("/page/{generatedPageId}")
    public ResponseEntity<?> getPageByGeneratedPageId(@PathVariable String generatedPageId) {
        try{
           Page page = publicProjectService.getPageByGeneratedPageId(generatedPageId);
           return ResponseEntity.ok(page);
        } catch (Exception e) {
          System.out.println("Error fetching page: " + e.getMessage());
         return ResponseEntity.status(500).body(Collections.singletonMap("error", e.getMessage()));
        }
    }


  @PostMapping("/{projectId}/favorite")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void favorite(
      @PathVariable String projectId,
      @RequestBody Map<String, Object> request,
      HttpServletRequest httpRequest
  ) {
     String token = httpRequest.getHeader("Authorization");
        if(token != null && token.startsWith("Bearer ")){
         token = token.substring(7);
        }
        String userId = null;
        if(token != null && sessionService.getUserIdByToken(token).equals(jwtUtil.validateTokenAndGetValue(token))){
            String resultUserId = sessionService.getUserIdByToken(token);
            userId = resultUserId;
     }
    if (userId == null) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED);
    Boolean favorite = Boolean.TRUE.equals(request.get("favorite"));
    publicProjectService.setFavorite(userId, projectId, favorite);
  }

  @PostMapping("/{projectId}/remix")
public ResponseEntity<?> remixProject(
    @PathVariable String projectId,
    HttpServletRequest request
) {

   Map<String, Object> response = new HashMap<>();
    String token = request.getHeader("Authorization");
    if (token != null && token.startsWith("Bearer ")) {
        token = token.substring(7);
    }

    String userId = null;
    if (token != null && sessionService.getUserIdByToken(token).equals(jwtUtil.validateTokenAndGetValue(token))) {
        userId = sessionService.getUserIdByToken(token);
    }
    if (userId == null) {
        response.put("message", "Unauthorized");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    try {
        String newProjectId = publicProjectService.remixProject(projectId, userId);
        if(newProjectId == null){
            response.put("message", "Project not found");
           return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        response.put("projectId", newProjectId);
        return ResponseEntity.ok(response);
    } catch (IllegalArgumentException notFound) {
       response.put("message", notFound.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    } catch (Exception e) {
        e.printStackTrace();
        response.put("message", "Failed to remix project");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
    
}
