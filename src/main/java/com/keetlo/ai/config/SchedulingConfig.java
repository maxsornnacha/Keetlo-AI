package com.keetlo.ai.config;

import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
@EnableScheduling
public class SchedulingConfig implements SchedulingConfigurer {

  @Bean
  public ThreadPoolTaskScheduler taskScheduler() {
    var ts = new ThreadPoolTaskScheduler();
    ts.setPoolSize(4); // run multiple jobs concurrently
    ts.setThreadNamePrefix("scheduler-");
    ts.setWaitForTasksToCompleteOnShutdown(true);
    ts.setAwaitTerminationSeconds(30);
    ts.initialize();
    return ts;
  }

  @Override
  public void configureTasks(ScheduledTaskRegistrar registrar) {
    registrar.setTaskScheduler(taskScheduler());
  }
}


