package com.keetlo.ai.job;

import java.time.ZoneId;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.keetlo.ai.service.SubscriptionService;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SubscriptionJob {
  private final SubscriptionService subscriptionService;
  private static final ZoneId BKK = ZoneId.of("Asia/Bangkok");

  public SubscriptionJob(SubscriptionService subscriptionService){
      this.subscriptionService = subscriptionService;
  }

  /** (4) Reset request tokens at midnight Bangkok time. */
  @Scheduled(cron = "0 0 0 * * *") /*every midnight*/
  public void resetTokensMidnight() {
    try {
      subscriptionService.resetDailyRequestTokens();
    } catch (Exception e) {
      log.error("resetTokensMidnight failed", e);
    }
  }

  /** (5) Downgrade expired packages to DEFAULT at midnight Bangkok time. */
  @Scheduled(cron = "0 */5 * * * *")
  public void downgradeExpiredMidnight() {
    try {
      subscriptionService.downgradeExpiredToDefault(BKK);
    } catch (Exception e) {
      log.error("downgradeExpiredMidnight failed", e);
    }
  }
}

