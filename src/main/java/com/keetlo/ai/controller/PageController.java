package com.keetlo.ai.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.keetlo.ai.dto.CreateMessageStreamReq;
import com.keetlo.ai.model.Page;
import com.keetlo.ai.service.MessageService;
import com.keetlo.ai.service.PageService;
import com.keetlo.ai.util.ImageTokenReplacer;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/page")
public class PageController {
    private final MessageService messageService;
    private final PageService pageService;
  private final WebClient openAiClient;
  private final WebClient ollamaClient;
    private final WebClient geminiClient;
    private final ObjectMapper objectMapper;
    private final ImageTokenReplacer imageTokenReplacer;

    public PageController(ObjectMapper objectMapper, PageService pageService,  MessageService messageService, WebClient openAiClient,
                        WebClient ollamaClient,  WebClient geminiClient,  ImageTokenReplacer imageTokenReplacer) {
        this.objectMapper = objectMapper;
        this.pageService = pageService;
        this.messageService = messageService;
        this.openAiClient = openAiClient;
        this.ollamaClient = ollamaClient;
        this.geminiClient = geminiClient;
        this.imageTokenReplacer = imageTokenReplacer;
        
    }

    private String buildSystemPrompt(){
       String systemPrompt = """
       You are Keetlo Assistant AI, a professional front-end developer.

Your task: generate complete, static HTML5 pages styled with Tailwind CSS (CDN), suitable for modern product/marketing sites (Vercel/Lovable/Stitch vibe).

## Output contract
- Emit JSON Lines (NDJSON): exactly one JSON object per page, in the same order as provided.
- No arrays. No commentary. No markdown. Only JSON objects, one per line.
- JSON must be directly parseable by JSON.parse and common Java JSON libraries (no trailing commas, BOM, or duplicate keys).

## Schema (strict)
Each object must match exactly:
{
  "label": "kebab-case-page-name",
  "path": "/url-path",
  "htmlContent": "<!DOCTYPE html>...complete HTML5 document..."
}

Rules:
- `label`: lowercase kebab-case, `/^[a-z0-9]+(-[a-z0-9]+)*$/`.
- `path`: starts with `/`, URL-safe chars (e.g., `/`, `/pricing`, `/account/login`).
- Only these three keys are allowed. Do not add any other keys.
- `htmlContent` is a JSON-escaped string (use `\"`, `\\n`, `\\t`, `\\uXXXX` as needed). Multi-line via `\n` is fine. Do not use backticks.

## Pages to generate
You will be given `pages: [...]` in the user input. Generate exactly one object per page in that list. Do not invent pages.

If `pages` is empty or missing: emit a single error object using the same schema:
{"label":"error","path":"/error","htmlContent":"<!DOCTYPE html><html lang='en'><head><meta charset='utf-8'/><meta name='viewport' content='width=device-width, initial-scale=1.0'/><title>Error</title></head><body>Missing pages</body></html>"}

## HTML requirements (flexible)
- Full HTML5 document with:
  - `<!DOCTYPE html><html lang="en">`
  - `<head>` contains:
    - `<meta charset="utf-8"/>`
    - `<meta name="viewport" content="width=device-width, initial-scale=1.0"/>`
    - `<title>[Page Title]</title>`
    - Google Fonts preconnects + Plus Jakarta Sans (preferred) or Space Grotesk
    - Tailwind CDN: `https://cdn.tailwindcss.com?plugins=forms,container-queries`
      - `<script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>`
    - Optional Lucide: `https://unpkg.com/lucide@latest`
      - `<style>.font-display{font-family:'Plus Jakarta Sans','sans-serif'}</style>`
  - `<body class="font-display min-h-screen flex flex-col bg-white text-gray-900">`
    - Header (sticky nav) with logo/icon + brand on the left, nav/actions on the right:
      `<header class="border-b"><nav class="container mx-auto px-6 py-4 flex justify-between items-center"><div class="flex items-center gap-2">[logo/icon + name]</div><div class="flex items-center gap-4">[nav/actions]</div></nav></header>`
  - <main> with purpose-fit sections (flexible):
    - `<footer class="border-t">` with Contact/About/Privacy/Terms; if Lucide is included, social icons may be used.
  - If Lucide is included, add `<script>lucide.createIcons();</script>` before `</body>`.

  ## Image token rules
- Do NOT output real image URLs.
- Use machine-readable tokens inside HTML attributes for images and backgrounds:
  - <img src="[[IMG:hero|gaming|1200x630|crop]]" .../>
  - <section style="background-image:url('[[IMG:banner|promotion|1600x900|cover]]')">
- Token syntax (strict): [[IMG:{slot}|{query}|{WxH}|{fit}]]
  - slot: hero|banner|card|thumb (free-form kebab allowed)
  - query: a short keyword or phrase (latin only), use hyphens instead of spaces (e.g., gaming-gear)
  - WxH: like 1200x630 (both integers)
  - fit: crop|fill|max (default: crop)
- Always provide alt text using the query in human words, e.g., alt="Gaming hero background".
- Keep tokens JSON-escaped as normal strings; do not break NDJSON rules.

## Background theme rule
- **Default background**: solid white (`bg-white` on `<body>`).
- **If the user explicitly requests a background or theme** (e.g., `dark`, `black`, `{"bg":"#111827"}`, `{"bg":"slate-900"}`, `{"bg":"linear-gradient(135deg,#0ea5e9 0%,#22d3ee 100%)"}`):
  - Use the requested background site-wide:
    - Tailwind color name: add `bg-[color]` to `<body>` (e.g., `bg-gray-900`) and ensure readable contrast with `text-black` or `text-gray-900`.
    - Hex/rgb/hsl/gradient: set `style="background:[value]"` on `<body>` and ensure readable contrast with `text-black` or `text-gray-900`.

## Anchor/link rule
- **All `<a>` elements must use `href="#"`** (stub links), including nav, CTAs, and footer links.
- For buttons styled as links, prefer `<a href="#" role="button">...</a>`.
- Use accessible labels (e.g., `aria-label` on icon-only anchors).

## Responsiveness (mobile → iPad/tablet → desktop)
- **Mobile-first** default. Enhance with Tailwind breakpoints (`sm`, `md` ≥768px, `lg` ≥1024px, `xl` as needed).
- Header/Nav:
  - Mobile: brand + hamburger; nav menu hidden by default. Provide a tiny inline toggle script that adds/removes `hidden` on the menu.
  - `md+`: show full nav inline; hide hamburger.

## Animations & transitions (deterministic, accessible)
- Every reveal element **must include**: `class="reveal opacity-0 translate-y-6 will-change-transform transition duration-500 ease-out"`. You may add other classes, but these **must** be present.
- Do **not** create any CSS rule that sets visibility/transform on `.reveal` (e.g., no `.reveal { opacity: ... }` or `.reveal { transform: ... }`).
- Required scroll-reveal script (append before </body>):
  <script>(function(){const r=window.matchMedia&&window.matchMedia('(prefers-reduced-motion: reduce)').matches;const els=[...document.querySelectorAll('.reveal')];if(!els.length)return;if(r){els.forEach(el=>el.classList.remove('opacity-0','translate-y-6'));return;}const io=new IntersectionObserver(es=>es.forEach(e=>{if(e.isIntersecting){e.target.classList.remove('opacity-0','translate-y-6');e.target.classList.add('opacity-100','translate-y-0');io.unobserve(e.target);}}),{threshold:0.15});els.forEach(el=>io.observe(el));})();</script>
- Optional reduced-motion CSS may **only** shorten durations; it must not change `.reveal` visibility:
  <style>@media (prefers-reduced-motion: reduce){*{animation-duration:.01ms!important;animation-iteration-count:1!important;transition-duration:.01ms!important;scroll-behavior:auto!important}}</style>

## Design guidance
- Mobile-first, responsive, coherent palette; ensure contrast readability with any custom background.
- Semantic HTML; accessible labels; correct roles for links/buttons.
- Do not add external scripts beyond Tailwind and optional Lucide.
- Use placeholders only; no third-party trackers.
- If any guidance conflicts with the page’s intent or clarity, favor the page’s intent (flexibility over quotas).

## Determinism & limits
- If content won’t fit token limits, emit pages in the given order and stop without inventing pages.
- If a rule conflicts with page purpose (e.g., login), omit non-essential marketing sections.
- Never include code fences, comments, or explanations outside the JSON objects.

## NDJSON hard rules (MUST follow)
- Emit **exactly one JSON object per line**. Never span an object across multiple lines.
- The object must have **only** these keys: "label", "path", "htmlContent" (no extras).
- **Never** output raw HTML outside of "htmlContent". If your draft would begin with "<!DOCTYPE html", STOP and re-emit as a single JSON object, with the HTML inside "htmlContent" (properly JSON-escaped).
- Inside "htmlContent", escape JSON correctly and also escape any `</tag>` as `<\\/tag>`.

## Final self-check before emitting a line
Answer only if all are true:
1) Line starts with "{" and ends with "}".
2) Keys are exactly "label","path","htmlContent".
3) "label" is kebab-case; "path" starts with "/".
4) "htmlContent" contains a full HTML5 doc beginning with "<!DOCTYPE html".
5) No backticks, no markdown, no commentary.
If any check fails, reformat and try again. Then emit the single line.
                """;

                return systemPrompt;

    }

    @PostMapping(value = "/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generatePages(@RequestBody CreateMessageStreamReq request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) auth.getPrincipal();
        String userInput = request.getInput();

        String useAI = "gemini"; 

   if (useAI.equals("gpt")) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", "gpt-4.1-mini");
        body.put("temperature", 0.5);
        //     body.put("model", "gpt-4.1");
        body.put("stream", true);
        ArrayNode msgs = objectMapper.createArrayNode();
        msgs.add(objectMapper.createObjectNode().put("role","system").put("content", this.buildSystemPrompt()));
        msgs.add(objectMapper.createObjectNode().put("role","user").put("content","User intent: " + userInput));
        body.set("messages", msgs);

      return openAiClient.post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(body.toString())
                .retrieve()
                .bodyToFlux(String.class)
                .retryWhen(
                reactor.util.retry.Retry.max(5)
                    .filter(t -> t instanceof WebClientResponseException.TooManyRequests)
                    .doBeforeRetry(retrySignal -> {
                        WebClientResponseException ex = (WebClientResponseException) retrySignal.failure();
                        String retryAfterHeader = ex.getHeaders().getFirst("Retry-After");
                        if (retryAfterHeader != null) {
                            long retryAfterSeconds = Long.parseLong(retryAfterHeader);
                            try {
                                Thread.sleep(retryAfterSeconds * 1000);
                            } catch (InterruptedException ignored) {}
                        }
                    })
               )
                .flatMap(chunk -> Flux.fromArray(chunk.split("\\r?\\n")))
                .filter(line -> !line.trim().isEmpty())
                .map(line -> line + "\n")
                .doOnComplete(() -> {
                    messageService.updateRequestToken(userId);
                });
    } else if (useAI.equals("gemini")) { 

    ObjectNode body = objectMapper.createObjectNode();

    // System instruction
    ObjectNode systemInstruction = objectMapper.createObjectNode();
    ArrayNode sysParts = objectMapper.createArrayNode();
    sysParts.add(objectMapper.createObjectNode().put("text", this.buildSystemPrompt()));
    systemInstruction.set("parts", sysParts);
    body.set("systemInstruction", systemInstruction);

    // User content
    ArrayNode contents = objectMapper.createArrayNode();
    ObjectNode userContent = objectMapper.createObjectNode().put("role", "user");
    ArrayNode userParts = objectMapper.createArrayNode();
    userParts.add(objectMapper.createObjectNode().put("text", "User intent: " + userInput));
    userContent.set("parts", userParts);
    contents.add(userContent);
    body.set("contents", contents);

    // (Optional but helpful) make sure we get plain text back
    ObjectNode genCfg = objectMapper.createObjectNode();
    genCfg.put("response_mime_type", "text/plain");
    body.set("generationConfig", genCfg);


    String geminiModel = "gemini-2.5-flash";
  return geminiClient.post()
      .uri(uriBuilder -> uriBuilder
         .path("/v1beta/models/{model}:streamGenerateContent")
        .queryParam("alt", "sse")           
        .build(geminiModel))  
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.TEXT_EVENT_STREAM)
      .bodyValue(body.toString())
      .retrieve()
      .bodyToFlux(String.class)
      // The API streams SSE lines like: "data: { ...json... }"
      .flatMap(chunk -> Flux.fromArray(chunk.split("\\r?\\n")))
      .filter(line -> !line.isBlank())
      .map(line -> line.startsWith("data:") ? line.substring(5).trim() : line.trim())
      .filter(data -> !data.isEmpty() && !data.equals("[DONE]"))
      .map(data -> {
        // Extract text from the chunk
        try {
          var node = objectMapper.readTree(data);
          var cands = node.path("candidates");
          if (cands.isArray() && cands.size() > 0) {
            var parts = cands.get(0).path("content").path("parts");
            if (parts.isArray()) {
              StringBuilder sb = new StringBuilder();
              for (var p : parts) sb.append(p.path("text").asText(""));
              // Keep your downstream expecting newline-delimited text
              return sb.toString();
            }
          }
        } catch (Exception ignore) {}
        return "";
      })
      .filter(s -> !s.isBlank())
      .map(s -> s + "\n")
      .doOnComplete(() -> messageService.updateRequestToken(userId));

    } else {
      // Example Ollama-style body; adjust to your server’s contract
      ObjectNode body = objectMapper.createObjectNode();
      body.put("model", "llama3:8b-instruct-q4_K_M");
       body.put("stream", true);
        ArrayNode msgs = objectMapper.createArrayNode();
        msgs.add(objectMapper.createObjectNode().put("role","system").put("content", this.buildSystemPrompt()));
        msgs.add(objectMapper.createObjectNode().put("role","user").put("content","User intent: " + userInput));
        body.set("messages", msgs);

      return ollamaClient.post()
                .uri("/v1/chat/completions") // now uses the ollama baseUrl bean
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(body.toString())
                .retrieve()
                .bodyToFlux(String.class)
                .retryWhen(
                reactor.util.retry.Retry.max(5)
                    .filter(t -> t instanceof WebClientResponseException.TooManyRequests)
                    .doBeforeRetry(retrySignal -> {
                        WebClientResponseException ex = (WebClientResponseException) retrySignal.failure();
                        String retryAfterHeader = ex.getHeaders().getFirst("Retry-After");
                        if (retryAfterHeader != null) {
                            long retryAfterSeconds = Long.parseLong(retryAfterHeader);
                            try {
                                Thread.sleep(retryAfterSeconds * 1000);
                            } catch (InterruptedException ignored) {}
                        }
                    })
               )
                .flatMap(chunk -> Flux.fromArray(chunk.split("\\r?\\n")))
                .filter(line -> !line.trim().isEmpty())
                .map(line -> line + "\n")
                .doOnComplete(() -> {
                    messageService.updateRequestToken(userId);
                });
    }

    }

    @PostMapping("/hydrate-images")
    public ResponseEntity<?> hydrateImages(@RequestBody Page request) {
       Map<String, Object> response = new HashMap<>();
        String html = request.getHtmlContent();
        if (html == null || html.isBlank()) {
            response.put("message", "htmlContent is required");
          return ResponseEntity.badRequest().body(response);
        }
        var cache = imageTokenReplacer.newRequestCache();
        String hydrated = imageTokenReplacer.replaceAll(html, cache);
         hydrated = imageTokenReplacer.ensureSrcset(hydrated);   
        response.put("message", "Success");
        response.put("hydratedHtmlContent", hydrated);
        return ResponseEntity.status(200).body(response);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadPage(@RequestBody Page request) {
        Map<String, Object> response = new HashMap<>();
        String projectMessageId = request.getProjectMessageId();
        String label = request.getLabel();
        String path = request.getPath();
        String htmlContent = request.getHtmlContent();
        Integer height = request.getHeight();
        Integer width = request.getWidth();
        Integer top = request.getTop();
        Integer left = request.getLeft();
        try {
            String generatedPageId = pageService.createPage(projectMessageId, label, path, htmlContent, height, width, top, left);
            if(generatedPageId == null){
                response.put("message", "Page can't be generated");
                return ResponseEntity.status(500).body(response);
            }

            return ResponseEntity.status(200).body(generatedPageId);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(500)
                    .body(Collections.emptyList());
        }
    }

    @GetMapping("/list/{projectId}")
    public ResponseEntity<?> getPagesByProject(@PathVariable String projectId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userId = (String) auth.getPrincipal();
            List<Page> pages = pageService.fetchingPages(projectId, userId);
            return ResponseEntity.ok(pages);
        } catch (Exception e) {
            System.out.println("Error fetching pages: " + e.getMessage());
            return ResponseEntity.status(500).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @MessageMapping("/update-page")
    public void updatePage(@Payload Page request) {
        try{
            pageService.updatePageByGeneratedPageId(request.getGeneratedPageId(), request.getHeight(), request.getWidth(), request.getTop(), request.getLeft());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

      @DeleteMapping("/delete/{generatedPageId}")
        public ResponseEntity<?> deletePage(@PathVariable String generatedPageId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userId = (String) auth.getPrincipal();
            boolean deleted = pageService.deletePageByGeneratedPageId(generatedPageId, userId);

            if (deleted) {
                return ResponseEntity.ok(Collections.singletonMap("message", "Page deleted successfully"));
            } else {
                return ResponseEntity.status(404).body(Collections.singletonMap("error", "Page not found or not owned by user"));
            }
        } catch (Exception e) {
            System.out.println("Error deleting page: " + e.getMessage());
            return ResponseEntity.status(500).body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}
