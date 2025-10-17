package com.keetlo.ai.job;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.keetlo.ai.service.OrderService;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OrderJob {
  private final OrderService orderService;

    public OrderJob(OrderService orderService){
      this.orderService = orderService;
  }

  /** (6) Every 30 minutes: expire PENDING orders older than 10 minutes. */
  @Scheduled(cron = "0 */5 * * * *") // every :00 and :30
  public void expirePendingOrders() {
    try {
      orderService.expireOldPendingOrders();
    } catch (Exception e) {
      log.error("expirePendingOrders failed", e);
    }
  }
}
