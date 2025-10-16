package com.keetlo.ai.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class SimpleTemplateRenderer {

    private String loadTemplate(String classpath) {
        try (var in = new ClassPathResource(classpath).getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load template: " + classpath, e);
        }
    }

    public String render(String classpathTemplate, Map<String, String> model) {
        String html = loadTemplate(classpathTemplate);
        for (var e : model.entrySet()) {
            html = html.replace("{{ " + e.getKey() + " }}", e.getValue() == null ? "" : e.getValue());
        }
        return html;
    }
}
