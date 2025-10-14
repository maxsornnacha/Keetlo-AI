// src/main/java/com/keetlo/ai/config/WebClientConfig.java
package com.keetlo.ai.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

  @Bean
  public WebClient openAiClient(
      WebClient.Builder builder,
      @Value("${openai.base.url:https://api.openai.com}") String baseUrl,
      @Value("${openai.api.key}") String apiKey
  ) {
    var http = HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30_000)
        .responseTimeout(Duration.ofMinutes(10))
        .doOnConnected(c -> {
          c.addHandlerLast(new ReadTimeoutHandler(300));
          c.addHandlerLast(new WriteTimeoutHandler(300));
        });

    return builder
        .baseUrl(baseUrl)
        .clientConnector(new ReactorClientHttpConnector(http))
        .defaultHeader("Authorization", "Bearer " + apiKey)
        .defaultHeader("Accept", "text/event-stream")
        .build();
  }

  @Bean
  public WebClient ollamaClient(
      WebClient.Builder builder,
      @Value("${ollama.base.url:http://194.195.90.160:11435}") String baseUrl
  ) {
    var http = HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30_000)
        .responseTimeout(Duration.ofMinutes(10))
        .doOnConnected(c -> {
          c.addHandlerLast(new ReadTimeoutHandler(300));
          c.addHandlerLast(new WriteTimeoutHandler(300));
        });

    return builder
        .baseUrl(baseUrl)
        .clientConnector(new ReactorClientHttpConnector(http))
        .defaultHeader("Accept", "text/event-stream")
        .build();
  }

@Bean
public WebClient geminiClient(
    WebClient.Builder builder,
  @Value("${gemini.base.url:https://generativelanguage.googleapis.com}") String baseUrl,
  @Value("${gemini.api.key}") String apiKey
) {
  var http = HttpClient.create()
      .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 300_000)
      .responseTimeout(Duration.ofMinutes(10))
      .doOnConnected(c -> {
        c.addHandlerLast(new ReadTimeoutHandler(300));
        c.addHandlerLast(new WriteTimeoutHandler(300));
      });

  return builder
      .baseUrl(baseUrl)
      .clientConnector(new ReactorClientHttpConnector(http))
      .defaultHeader("x-goog-api-key", apiKey) 
      .defaultHeader("Accept", "text/event-stream")
      .build();
}
}
