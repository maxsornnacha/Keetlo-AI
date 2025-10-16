// composables/useFormat.ts
export function useFormatTime() {
  const toDate = (v?: string | number | Date) => {
    if (!v) return null;
    const d = new Date(v);
    return Number.isNaN(d.getTime()) ? null : d;
  };

  const formatDate = (v?: string | number | Date, locale?: string) => {
    const d = toDate(v);
    if (!d) return "";
    return new Intl.DateTimeFormat(locale, { dateStyle: "medium" }).format(d);
  };

  const formatTime = (v?: string | number | Date, locale?: string) => {
    const d = toDate(v);
    if (!d) return "";
    return new Intl.DateTimeFormat(locale, { timeStyle: "short" }).format(d);
  };

  const formatDateTime = (v?: string | number | Date, locale?: string) => {
    const d = toDate(v);
    if (!d) return "";
    return new Intl.DateTimeFormat(locale, {
      dateStyle: "medium",
      timeStyle: "short",
    }).format(d);
  };

  // If you want UTC instead of local time:
  const formatDateTimeUTC = (v?: string | number | Date, locale?: string) => {
    const d = toDate(v);
    if (!d) return "";
    return new Intl.DateTimeFormat(locale, {
      dateStyle: "medium",
      timeStyle: "short",
      timeZone: "UTC",
    }).format(d);
  };

  return { formatDate, formatTime, formatDateTime, formatDateTimeUTC };
}
