package com.keetlo.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Service
public class AiService {

    private static final String AI_URL = "https://ai.keetlo.com/api/generate";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient webClient = WebClient.builder().baseUrl(AI_URL).build();

    public String generateProjectJson(String userInput) {
        String jsonBody = "{\n" +
                "  \"model\":\"scb10x/typhoon2.1-gemma3-4b\",\n" +
                "  \"prompt\":\"User input: " + userInput
                + "\\nTask: Create a project based on this input. Output a JSON object with fields: title, description, type, tags. Keep JSON strictly valid.\"\n"
                +
                "}";

        StringBuilder rawResponse = new StringBuilder();

        // Stream response and accumulate text
        Flux<String> responseFlux = webClient.post()
                .header("Content-Type", "application/json")
                .bodyValue(jsonBody)
                .retrieve()
                .bodyToFlux(String.class);

        responseFlux.doOnNext(rawResponse::append)
                .blockLast(); // wait until all chunks are received

        String finalJson = rawResponse.toString();

        // Try parsing the accumulated text as JSON
        try {
            JsonNode node = objectMapper.readTree(finalJson);
            if (node.has("response")) {
                System.out.println(node.get("response").asText());
                return node.get("response").asText();
            }
        } catch (Exception e) {
            // If parsing fails, fallback to returning raw text
            e.printStackTrace();
        }

        return finalJson; // return the raw response if JSON parsing fails
    }

}
