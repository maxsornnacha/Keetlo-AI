package com.keetlo.ai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Service
public class UnsplashService {

  private final String accessKey;
  private final WebClient http;

  public record Photo(String alt_description, User user, Urls urls) {}
  public record User(String name, Links links) {}
  public record Links(String html) {}
  public record Urls(String raw) {}

  public UnsplashService(@Value("${unsplash.accessKey:}") String keyFromProp) {
    this.accessKey = keyFromProp;

    if (this.accessKey.isBlank()) {
      throw new IllegalStateException("Unsplash access key missing. Set 'unsplash.accessKey' or env UNSPLASH_ACCESS_KEY");
    }

    // สร้าง WebClient แบบ dedicated และ "บังคับ" ใส่ Authorization ทุกครั้ง
    this.http = WebClient.builder()
        .baseUrl("https://api.unsplash.com")
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .filter((req, next) -> {
          ClientRequest mutated = ClientRequest.from(req)
              .headers(h -> {
                h.remove(HttpHeaders.AUTHORIZATION);
                h.set(HttpHeaders.AUTHORIZATION, "Client-ID " + accessKey);
              })
              .build();
          return next.exchange(mutated);
        })
        .build();
  }

  public Photo fetchRandom(String query) {
    return http.get()
        .uri(u -> u.path("/photos/random")
            .queryParam("query", query)
            .queryParam("orientation", "landscape")
            .build())
        .retrieve()
        .onStatus(HttpStatusCode::isError, resp ->
            resp.bodyToMono(String.class).flatMap(body -> {
              System.err.println("[Unsplash error] status=" + resp.statusCode() + " body=" + body);
              return Mono.error(new WebClientResponseException(
                  "Unsplash error: " + body,
                  resp.statusCode().value(),
                  resp.statusCode().toString(), null, null, null));
            }))
        .bodyToMono(Photo.class)
        .block();
  }

  /** สร้าง images.unsplash.com URL ด้วยพารามิเตอร์ปรับขนาด/คุณภาพ */
  public String buildSrc(String rawUrl, int w, int h, String fit, int q, int dpr) {
    return UriComponentsBuilder.fromUriString(rawUrl)
        .queryParam("auto", "format")
        .queryParam("w", w)
        .queryParam("h", h)
        .queryParam("fit", (fit == null || fit.isBlank()) ? "crop" : fit)
        .queryParam("q", q)
        .queryParam("dpr", dpr)
        .build(true).toUriString();
  }
}
