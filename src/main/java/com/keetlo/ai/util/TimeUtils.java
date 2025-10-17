package com.keetlo.ai.util;

import java.time.*;

public final class TimeUtils {
  private TimeUtils() {}

  /** Returns true if end is not null and now >= end (expired). */
  public static boolean isExpired(Instant end) {
    if (end == null) return false;            // decide your policy for nulls
    return Instant.now().compareTo(end) >= 0; // now >= end
  }

  /** same, but for LocalDateTime with a ZoneId (end interpreted in that zone). */
  public static boolean isExpired(LocalDateTime end, ZoneId zone) {
    if (end == null) return false;
    ZonedDateTime endZ = end.atZone(zone);
    ZonedDateTime nowZ = ZonedDateTime.now(zone);
    return !endZ.isAfter(nowZ);               // now >= end
  }

  /** Convenience for ISO-8601 strings like 2025-10-15T10:09:54 or with offset. */
  public static boolean isExpired(String isoEnd, ZoneId zone) {
    if (isoEnd == null || isoEnd.isBlank()) return false;
    try {
      // Try Instant/Offset first (e.g., 2025-10-15T10:09:54Z or +07:00)
      return isExpired(Instant.parse(isoEnd));
    } catch (Exception ignored) { /* fall through */ }

    try {
      // Fallback to local date-time (no zone info)
      LocalDateTime ldt = LocalDateTime.parse(isoEnd);
      return isExpired(ldt, zone);
    } catch (Exception e) {
      // If unparsable, choose your policy (here: not expired)
      return false;
    }
  }


 public static boolean isExpired(LocalDate endDate, ZoneId zone) {
    if (endDate == null) return false;
    ZonedDateTime cutoff = endDate.plusDays(1).atStartOfDay(zone); // first moment AFTER the end day
    return !ZonedDateTime.now(zone).toInstant().isBefore(cutoff.toInstant());
  }

  /** Returns remaining duration until end; negative/zero means expired or at end. */
  public static Duration remainingUntil(Instant end) {
    if (end == null) return Duration.ofDays(Long.MAX_VALUE); // "no end"
    return Duration.between(Instant.now(), end);
  }
}
