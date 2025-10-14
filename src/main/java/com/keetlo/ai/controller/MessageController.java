package com.keetlo.ai.controller;

import java.time.Duration;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.keetlo.ai.dto.CreateMessageStreamReq;
import com.keetlo.ai.dto.StoreAiMessageReq;
import com.keetlo.ai.model.Message;
import com.keetlo.ai.service.MessageService;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/message")
public class MessageController {
        private final MessageService projectMessageService;
        private final WebClient ollamaClient;
        private final WebClient openAiClient;
        private final ObjectMapper objectMapper;

        public MessageController(ObjectMapper objectMapper, MessageService projectMessageService,
                        WebClient ollamaClient, WebClient openAiClient) {
                this.objectMapper = objectMapper;
                this.projectMessageService = projectMessageService;
                this.ollamaClient = ollamaClient;
                this.openAiClient = openAiClient;
        }

        @PostMapping(value = "/user", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public Flux<String> createMessageStream(@RequestBody CreateMessageStreamReq request)
                        throws JsonProcessingException {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String userId = (String) auth.getPrincipal();
                String userInput = request.getInput();
                String projectId = request.getProjectId();
                Boolean firstTry = request.isFirstTry();

                Boolean isOk = projectMessageService.checkRequestToken(userId);
                if (!isOk) {
                        String text = "<div class='text-red-500'>You have reached your request limit.</div>\n";
                        projectMessageService.createMessage(
                                        request.getProjectId(),
                                        userId,
                                        "AI",
                                        text);
                        return Flux.just(text);
                }
                projectMessageService.updateRequestToken(userId);

                if (!firstTry) {
                        String newMessageId = projectMessageService.createMessage(projectId, userId, "USER",
                                        userInput);
                        if (newMessageId == null) {
                                String text = "<div class='There is something error, please try again later.</div>\n";
                                projectMessageService.createMessage(
                                                request.getProjectId(),
                                                userId,
                                                "AI",
                                                text);
                                return Flux.just(text);
                        }
                }

                List<Message> previousMessages = projectMessageService.getProjectMessagesByProjectId(projectId);
                StringBuilder contextBuilder = new StringBuilder();

                int size = previousMessages.size();
                // Start index so we only take the last 4 messages (or fewer if less exist)
                int startIndex = Math.max(size - 4, 0);

                for (int i = startIndex; i < size; i++) {
                        Message message = previousMessages.get(i);
                        String sender = message.getRole().equals("USER") ? "User" : "AI";
                        contextBuilder.append(sender).append(": ").append(message.getMessage()).append("\n");
                }

            String prompt = """
                User input log is: (%s)

                The user input is: (%s)

                You are Keetlo Assistant AI, a professional web strategist and front-end developer. \
                Write in Thai or English to match the most recent user input.

                OUTPUT RULES (stream-safe, plain text only):
                1) First line EXACTLY:
                To design a comprehensive website related to the user input, we might consider the following pages:
                2) Then list the pages to generate. Output a maximum of 1 pages (1). The number pages will be acordding to user input, ONE PER LINE, each formatted as:
                Page Name - Full description
                3) Do NOT use HTML, markdown, or entities (no <br/>, no &nbsp;). Plain text only.
                4) Do NOT repeat pages already present in the user inputs.
                5) No extra blank lines; keep each item under 200 characters; never split an item across multiple lines.
                6) End the response with a single newline.

                ## Description Rule
                - Do Not use HTML, markdown, entities or Special characters in description 
             """.formatted(contextBuilder.toString(), userInput);

                String useAI = "gpt";

                if (useAI.equals("gpt")) {
                        ObjectNode body = objectMapper.createObjectNode();
                        body.put("model", "gpt-4o-mini");
                        body.put("stream", true);

                        ArrayNode messages = objectMapper.createArrayNode();
                        messages.add(objectMapper.createObjectNode()
                                        .put("role", "system")
                                        .put("content", prompt));
                        messages.add(objectMapper.createObjectNode()
                                        .put("role", "user")
                                        .put("content", userInput));
                        body.set("messages", messages);

                        String jsonBody = objectMapper.writeValueAsString(body);

                        return openAiClient
                                        .post()
                                        .uri("/v1/chat/completions")
                                        .header("Content-Type", "application/json")
                                        .accept(org.springframework.http.MediaType.TEXT_EVENT_STREAM)
                                        .bodyValue(jsonBody)
                                        .retrieve()
                                        .bodyToFlux(String.class)
                                        // OpenAI SSE frames look like: "data: {...}" and end with "data: [DONE]"
                                        .flatMap(chunk -> Flux.fromArray(chunk.split("\\r?\\n")))
                                        .filter(line -> !line.isBlank())
                                        .map(line -> line.startsWith("data:") ? line.substring(5).trim() : line.trim())
                                        .filter(data -> !data.isEmpty() && !data.equals("[DONE]"))
                                        .map(data -> {
                                                try {
                                                        var node = objectMapper.readTree(data);
                                                        // text delta is here:
                                                        var delta = node.path("choices").get(0).path("delta")
                                                                        .path("content").asText("");
                                                        return delta;
                                                } catch (Exception ignore) {
                                                        return "";
                                                }
                                        })
                                        .filter(s -> !s.isBlank())
                                        .map(s -> s + "\n");

                } else {
                        ObjectNode bodyNode = objectMapper.createObjectNode();
                        bodyNode.put("model", "llama3:8b-instruct-q4_K_M");
                        bodyNode.put("prompt", prompt);

                        String jsonBody = objectMapper.writeValueAsString(bodyNode);
                        return ollamaClient
                                        .post()
                                        .uri("/api/generate")
                                        .header("Content-Type", "application/json")
                                        .bodyValue(jsonBody)
                                        .retrieve()
                                        .bodyToFlux(String.class)
                                        // .map(chunk -> chunk + "\n");
                                        .map(chunk -> {
                                                try {
                                                        var node = objectMapper.readTree(chunk);
                                                        return node.has("response")
                                                                        ? node.get("response").asText() + "\n"
                                                                        : "";
                                                } catch (Exception e) {
                                                        e.printStackTrace();
                                                        return "";
                                                }
                                        })
                                        .delayElements(Duration.ofMillis(50));

                }
        }

        @PostMapping("/ai")
        public ResponseEntity<?> storeAiMessage(@RequestBody StoreAiMessageReq request) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String userId = (String) auth.getPrincipal();
                String newMessageId = projectMessageService.createMessage(
                                request.getProjectId(),
                                userId,
                                "AI",
                                request.getMessage());

                if (newMessageId != null) {
                        return ResponseEntity.ok().body(newMessageId);
                } else {
                        return ResponseEntity.status(500).body("Failed to store AI message.");
                }
        }
}
