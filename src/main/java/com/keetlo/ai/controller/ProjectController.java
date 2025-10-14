package com.keetlo.ai.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keetlo.ai.model.Project;
import com.keetlo.ai.service.MessageService;
import com.keetlo.ai.service.ProjectService;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/project")
public class ProjectController {

        private final MessageService projectMessageService;

        private final ProjectService projectService;
        private final WebClient webClient = WebClient.builder()
                        .baseUrl("http://194.195.90.160:11435/api/generate")
                        .build();
        private final ObjectMapper objectMapper;

        public ProjectController(ObjectMapper objectMapper, ProjectService projectService,
                        MessageService projectMessageService) {
                this.objectMapper = objectMapper;
                this.projectService = projectService;
                this.projectMessageService = projectMessageService;
        }

        @PostMapping("/create")
        public ResponseEntity<Map<String, String>> createProject(@RequestBody Map<String, String> request) {
                Map<String, String> response = new HashMap<>();
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String userId = (String) auth.getPrincipal();
                String userInput = request.get("input");

                // สร้าง projectId
                Project project = new Project();
                String projectId = project.createProjectId();
                projectService.createProject(projectId, userId);
                projectMessageService.createMessage(projectId, userId, "USER", userInput);
                project.setProjectId(projectId);
                project.setTitle("Pending...");
                project.setDescription("creating");

                // เรียก AI แบบ async (background)
                generateProjectAsync(userInput, projectId, userId);

                // ตอบกลับทันที
                response.put("status", "creating");
                response.put("projectId", projectId);
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
        }

        @Async
        public void generateProjectAsync(String userInput, String projectId, String userId) {

                String jsonBody = "{\n" +
                                "  \"model\":\"llama3:8b-instruct-q4_K_M\",\n" +
                                "  \"prompt\":\"User input: " + userInput
                                + "\\nTask: Create a project based on this input. Output a JSON stringify with fields: title, description, type, tags. Keep JSON stringify strictly valid and Do **not** include explanations, text, bullet points, or markdown.\"\n"
                                +
                                "}";

                try {
                        Flux<String> aiFlux = webClient.post()
                                        .header("Content-Type", "application/json")
                                        .bodyValue(jsonBody)
                                        .retrieve()
                                        .bodyToFlux(String.class);
                        StringBuilder fullResponse = new StringBuilder();
                        aiFlux.subscribe(chunk -> {
                                try {
                                        JsonNode node = objectMapper.readTree(chunk);
                                        fullResponse.append(node.get("response").asText());
                                } catch (Exception e) {
                                        e.printStackTrace();
                                }
                        }, error -> {
                                error.printStackTrace();
                        }, () -> {
                                try {
                                        String json = fullResponse.toString()
                                                        .replace("```json", "")
                                                        .replace("```", "")
                                                        .trim();
                                        int start = json.indexOf('{');
                                        int end = json.lastIndexOf('}');
                                        if (start != -1 && end != -1 && end > start) {
                                            json = json.substring(start, end + 1);
                                         }
                                        Project project = objectMapper.readValue(json, Project.class);
                                        projectService.updateProjectAiGenerate(
                                                        projectId,
                                                        userId,
                                                        project.getTitle(),
                                                        project.getDescription(),
                                                        project.getType(),
                                                        project.getTags());
                                } catch (Exception e) {
                                        e.printStackTrace();
                                }
                        });
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        @GetMapping("/{projectId}")
        public ResponseEntity<?> getProject(@PathVariable String projectId) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String userId = (String) auth.getPrincipal();
                Project project = projectService.getProjectByProjectId(projectId, userId);
                if (project == null) {
                        return ResponseEntity.status(404).body("The project not found");
                }
                return ResponseEntity.ok(project);
        }

@GetMapping("/me")
public ResponseEntity<Map<String, Object>> getMyProjects(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(required = false) String search,             // search by title or description
        @RequestParam(required = false) String startDate,          // filter from date (YYYY-MM-DD)
        @RequestParam(required = false) String endDate             // filter to date (YYYY-MM-DD)
) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String userId = (String) auth.getPrincipal();
    Map<String, Object> response = new HashMap<>();
    try {
        Map<String, Object> result = projectService.getProjectsByUserIdPaginated(userId, page, pageSize, search, startDate, endDate);
        return ResponseEntity.ok(result);
    } catch (Exception e) {
        e.printStackTrace();
        response.put("projects", Collections.emptyList());
        response.put("totalPages", 0);
        response.put("currentPage", page);
        return ResponseEntity.status(500).body(response);
    }
}


    @PostMapping("/update-public")
    public ResponseEntity<?> updatePublic(@RequestBody Project request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) auth.getPrincipal();
        Map<String, String> response = new HashMap<>();
        try {
            Boolean updated = projectService.updatePublicByProjectIdAndUserId(
                    request.getIsPublic(),
                    request.getIndexPage(),
                    request.getProjectId(),
                    userId
            );

            if (updated) {
                return ResponseEntity.ok("Project updated successfully");
            } else {
                response.put("message", "Failed to update project");
                return ResponseEntity.status(400).body(response);
            }
        } catch (Exception error) {
            response.put("message", "Server interval error: " + error.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/update")
    public ResponseEntity<?> editProject(@RequestBody Project request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) auth.getPrincipal();

        try {
            boolean updated = projectService.updateProjectDetails(
                    request.getProjectId(),
                    userId,
                    request.getTitle(),
                    request.getDescription(),
                    request.getType(),
                    request.getTags()
            );

            if (updated) {
                return ResponseEntity.ok(Collections.singletonMap("message", "Project updated successfully"));
            } else {
                return ResponseEntity.status(404).body(Collections.singletonMap("error", "Project not found or not owned by user"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

        @DeleteMapping("/delete/{projectId}")
        public ResponseEntity<?> deleteProject(@PathVariable String projectId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) auth.getPrincipal();

        try {
            boolean deleted = projectService.deleteProjectByProjectId(projectId, userId);

            if (deleted) {
                return ResponseEntity.ok(Collections.singletonMap("message", "Project deleted successfully"));
            } else {
                return ResponseEntity.status(404).body(Collections.singletonMap("error", "Project not found or not owned by user"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}
