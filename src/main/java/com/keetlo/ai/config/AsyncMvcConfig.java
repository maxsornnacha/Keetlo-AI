// src/main/java/.../config/AsyncMvcConfig.java
package com.keetlo.ai.config;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AsyncMvcConfig implements WebMvcConfigurer {

  @Bean(name = "mvcTaskExecutor")
  public TaskExecutor mvcTaskExecutor() {
    ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
    ex.setThreadNamePrefix("mvc-async-");
    ex.setCorePoolSize(Math.max(8, Runtime.getRuntime().availableProcessors() * 2));
    ex.setMaxPoolSize(64);        // tune for your traffic
    ex.setQueueCapacity(2000);    // use a bounded queue
    ex.setAllowCoreThreadTimeOut(true);
    ex.initialize();
    return ex;
  }

  @Override
  public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
    configurer.setTaskExecutor((AsyncTaskExecutor) mvcTaskExecutor());
    configurer.setDefaultTimeout(Duration.ofMinutes(2).toMillis()); // per request
  }
}
