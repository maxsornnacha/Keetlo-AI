package com.keetlo.ai.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.keetlo.ai.model.File;
import com.keetlo.ai.model.Message;
import com.keetlo.ai.service.FileService;
import com.keetlo.ai.service.MessageService;
import com.keetlo.ai.util.FileUtil;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/message")
public class MessageController {
        private final MessageService projectMessageService;
        private final FileService fileService;
        private final WebClient openAiClient;
        private final ObjectMapper objectMapper;
        private final FileUtil fileUtil;

        public MessageController(ObjectMapper objectMapper, MessageService projectMessageService,
                        WebClient openAiClient, FileUtil fileUtil, FileService fileService) {
                this.objectMapper = objectMapper;
                this.projectMessageService = projectMessageService;
                this.openAiClient = openAiClient;
                this.fileUtil = fileUtil;
                this.fileService = fileService;
        }

        @PostMapping("/generate-webpage-intention")
        public ResponseEntity<?> classifyGenerateIntention(@RequestBody Map<String, Object> body) {
                // Accept { "input": "..." } or { "message": "..." }
                final String input = (String) (body.getOrDefault("input", body.getOrDefault("message", "")));
                final Map<String, Object> resp = new HashMap<>();

                if (input == null || input.isBlank()) {
                        resp.put("message", "input is required");
                        return ResponseEntity.badRequest().body(resp);
                }

                try {
                        // ----- Build OpenAI request -----
                        // We’ll force JSON output with a small schema.
                        ObjectNode req = objectMapper.createObjectNode();
                        req.put("model", "gpt-4o-mini");
                        req.put("temperature", 0.0);

                        // Ask for JSON only
                        ObjectNode responseFormat = objectMapper.createObjectNode();
                        responseFormat.put("type", "json_object");
                        req.set("response_format", responseFormat);

                        ArrayNode messages = objectMapper.createArrayNode();

                        // System instructions
                        messages.add(
                                        objectMapper.createObjectNode()
                                                        .put("role", "system")
                                                        .put("content",
                                                                        """
                                                                                        You are a classifier. Decide if the user's message asks to create/generate/build a web page (HTML/Tailwind/landing page/website).
                                                                                        Return ONLY valid JSON with this exact shape:
                                                                                        {
                                                                                          "shouldGenerate": boolean,
                                                                                          "intent": "generate_web_page" | "chat_question" | "other",
                                                                                          "confidence": number,  // 0..1
                                                                                          "reason": string
                                                                                        }
                                                                                        Guidelines:
                                                                                        - If the user explicitly asks to build/make/generate a page/landing/website, or gives page sections/HTML requirements, mark generate_web_page.
                                                                                        - If it's general questions about tech/design without an explicit build request, mark chat_question.
                                                                                        - Otherwise mark other.
                                                                                        """));

                        // User message
                        messages.add(
                                        objectMapper.createObjectNode()
                                                        .put("role", "user")
                                                        .put("content", input));

                        req.set("messages", messages);

                        // ----- Call OpenAI -----
                        String raw = openAiClient
                                        .post()
                                        .uri("/v1/chat/completions")
                                        .header("Content-Type", "application/json")
                                        .accept(MediaType.APPLICATION_JSON)
                                        .bodyValue(objectMapper.writeValueAsString(req))
                                        .retrieve()
                                        .bodyToMono(String.class)
                                        .block();

                        // ----- Parse model JSON -----
                        // choices[0].message.content will be a JSON string we asked for
                        var root = objectMapper.readTree(raw);
                        String content = root.path("choices").get(0).path("message").path("content").asText("");

                        // Fallback guard
                        if (content.isBlank()) {
                                Map<String, Object> fallback = Map.of(
                                                "shouldGenerate", false,
                                                "intent", "other",
                                                "confidence", 0.2,
                                                "reason", "Empty model response");
                                return ResponseEntity.ok(fallback);
                        }

                        // Parse the JSON the model returned
                        Map<String, Object> modelJson = objectMapper.readValue(content, Map.class);

                        // You can rename keys on the way out if you like
                        Map<String, Object> out = new HashMap<>();
                        out.put("message", "success");
                        out.put("input", input);
                        out.put("shouldGenerate", modelJson.getOrDefault("shouldGenerate", false));
                        out.put("intent", modelJson.getOrDefault("intent", "other"));
                        out.put("confidence", modelJson.getOrDefault("confidence", 0.0));
                        out.put("reason", modelJson.getOrDefault("reason", ""));

                        return ResponseEntity.ok(out);

                } catch (Exception e) {
                        Map<String, Object> error = new HashMap<>();
                        error.put("message", "Classification failed");
                        error.put("error", e.getMessage());
                        return ResponseEntity.status(500).body(error);
                }
        }

        @PostMapping(value = "/user", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public Flux<String> createMessageStream(@RequestBody CreateMessageStreamReq request)
                        throws JsonProcessingException {
                try {
                        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                        String userId = (String) auth.getPrincipal();
                        String userInput = request.getInput();
                        String projectId = request.getProjectId();
                        List<File> files = request.getFiles();
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
                        // projectMessageService.updateRequestToken(userId);

                        if (!firstTry) {
                                String newMessageId = projectMessageService.createMessage(projectId, userId, "USER",
                                                userInput);
                                // สร้าง File
                                if (files != null && !files.isEmpty() && files.size() > 0) {
                                        for (File f : files) {
                                                String base64File = f.getBase64();

                                                if (base64File != null && !base64File.isEmpty()
                                                                && fileUtil.isBase64(base64File)) {
                                                        String mime = (base64File != null)
                                                                        ? fileUtil.mimeFromDataUrl(base64File)
                                                                        : f.getFileType();

                                                        String ext = fileUtil.pickExt(mime, f.getFileName());
                                                        String uploadDir = "/files/projects/" + newMessageId + "/";
                                                        String fileName = "file_" + System.currentTimeMillis() + "."
                                                                        + ext;
                                                        String filePath = uploadDir + fileName;
                                                        try {
                                                                fileUtil.saveBase64File(base64File, filePath);
                                                        } catch (IOException e) {
                                                        }
                                                        f.setFileUrl("/files/projects/" + newMessageId + "/"
                                                                        + fileName);

                                                        fileService.createFile(newMessageId, f.getFileName(),
                                                                        f.getFileType(), f.getFileSize(),
                                                                        f.getFileUrl());
                                                }
                                        }
                                }
                                if (newMessageId == null) {
                                        String text = "<div class='There is something error, please try again later.</div>\n";
                                        projectMessageService.createMessage(
                                                        request.getProjectId(),
                                                        userId,
                                                        "AI",
                                                        text);
                                        return Flux.just(text);
                                }
                                // Upload Files into DB
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
                                           1) Answer is flexible, good response for user input, and response only 1 language not combine many languages
                                           2) if user input is about website-page creation, then summerize the user input to redeclare what user's brief and then suggest list the pages to generate. Output a maximum of 1 pages (1). The number pages will be acordding to user input, ONE PER LINE, each formatted as:
                                           Defining page name - Defining full description
                                           4) Do NOT use HTML, markdown, or entities (no <br/>, no &nbsp;). Plain text only.
                                           5) Do NOT repeat pages already present in the user inputs.
                                           6) No extra blank lines; keep each item under 10000 characters; never split an item across multiple lines.
                                           7) End the response with a single newline.

                                           ## Description Rule
                                           - Do Not use HTML, markdown, entities or Special characters in description
                                        """
                                        .formatted(contextBuilder.toString(), userInput);

                        ObjectNode body = objectMapper.createObjectNode();
                        body.put("model", "gpt-4o-mini");
                        body.put("stream", true);
                        body.put("temperature", 0.7);

                        ArrayNode messages = objectMapper.createArrayNode();

                        // 1) system message
                        messages.add(objectMapper.createObjectNode()
                                        .put("role", "system")
                                        .put("content", prompt));

                        // 2) user message with mixed parts
                        ArrayNode userContent = objectMapper.createArrayNode();
                        if (userInput != null && !userInput.isBlank()) {
                                userContent.add(objectMapper.createObjectNode()
                                                .put("type", "text")
                                                .put("text", userInput));
                        }

                        // Expecting frontend to send: List<FileDto> files (fileName, fileType,
                        // fileSize, base64)
                        if (files != null)
                                for (File f : files) {
                                        String dataUrl = f.getBase64(); // MUST be data:image/...;base64,....
                                        if (fileUtil.isImageDataUrl(dataUrl)) {
                                                ObjectNode imgUrl = objectMapper.createObjectNode();
                                                imgUrl.put("url", dataUrl);
                                                imgUrl.put("detail", "high");

                                                ObjectNode imgPart = objectMapper.createObjectNode();
                                                imgPart.put("type", "image_url");
                                                imgPart.set("image_url", imgUrl);
                                                userContent.add(imgPart);
                                        }
                                }

                        // Guard: if user sent neither text nor files, give a friendly nudge
                        if (userContent.isEmpty()) {
                                userContent.add(objectMapper.createObjectNode()
                                                .put("type", "text")
                                                .put("text", "No input provided. Please add text and/or files."));
                        }

                        // Finally add the user message (IMPORTANT: set content = array, not string)
                        ObjectNode userMsg = objectMapper.createObjectNode();
                        userMsg.put("role", "user");
                        userMsg.set("content", userContent);
                        messages.add(userMsg);
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
                                        .map(line -> line.startsWith("data:") ? line.substring(5) : line)
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
                } catch (Exception e) {
                        String msg = "Error generating the pages: " + e.getMessage();
                        return Flux.error(new RuntimeException(msg, e));
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
