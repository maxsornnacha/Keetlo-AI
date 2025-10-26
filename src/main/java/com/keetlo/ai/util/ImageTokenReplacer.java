package com.keetlo.ai.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.*;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.keetlo.ai.service.UnsplashService;

@Component
public class ImageTokenReplacer {

private static final Pattern TOKEN = Pattern.compile(
    "\\[\\[IMG:([^\\|\\]]+)\\|([a-z0-9-]+)\\|(\\d+)x(\\d+)(?:\\|([a-z-]+))?\\]\\]",
    Pattern.CASE_INSENSITIVE
);

private static String normalizeFit(String fit) {
  if (fit == null || fit.isBlank()) return "crop";
  fit = fit.toLowerCase();
  switch (fit) {
    case "cover":   return "crop"; // cover ≈ ครอบ/ตัด ให้เต็มกรอบ
    case "contain": return "max";  // contain ≈ ย่อให้พอดีไม่ตัด
    case "fill":    return "fill";
    case "crop":    return "crop";
    case "max":     return "max";
    default:        return "crop";
  }
}

  private static final Pattern IMG_NO_SRCSET = Pattern.compile(
      "(<img\\b[^>]*?\\s)src=\"(https://images\\.unsplash\\.com/[^\"<>]+)\"([^>]*?)(?<!\\s/)>",
      Pattern.CASE_INSENSITIVE);

  private final UnsplashService unsplash;

  public ImageTokenReplacer(UnsplashService unsplash) {
    this.unsplash = unsplash;
  }

  /** cache ต่อคำขอ */
  public RequestCache newRequestCache() {
    return new RequestCache();
  }

  public static final class RequestCache {
    // key: slot|query → photo rawUrl ที่เลือกแล้ว
    private final Map<String, String> rawUrlByKey = new ConcurrentHashMap<>();
    // (ถ้าจะทำเครดิต) เก็บชื่อ/ลิงก์ผู้ถ่ายได้ที่นี่
    private final Map<String, String> authorNameByKey = new ConcurrentHashMap<>();
    private final Map<String, String> authorUrlByKey = new ConcurrentHashMap<>();

    public String getRawUrl(String key) { return rawUrlByKey.get(key); }
    public void putRawUrl(String key, String rawUrl) { rawUrlByKey.put(key, rawUrl); }

    public void putAuthor(String key, String name, String url) {
      authorNameByKey.put(key, name);
      authorUrlByKey.put(key, url);
    }
    public String getAuthorName(String key) { return authorNameByKey.get(key); }
    public String getAuthorUrl(String key) { return authorUrlByKey.get(key); }
  }

  /** แทนทุกโทเคน IMG ใน HTML */
 public String replaceAll(String html, RequestCache cache) {
  Matcher m = TOKEN.matcher(html);
  StringBuffer sb = new StringBuffer();

  while (m.find()) {
    String slot = m.group(1).toLowerCase();
    String querySlug = m.group(2).toLowerCase();
    int w = Integer.parseInt(m.group(3));
    int h = Integer.parseInt(m.group(4));
    String fit = normalizeFit(m.group(5)); // <<— ใช้ normalize

    String key = slot + "|" + querySlug;

    String rawUrl = cache.getRawUrl(key);
    if (rawUrl == null) {
      try {
        var photo = unsplash.fetchRandom(querySlug.replace('-', ' '));
        rawUrl = photo.urls().raw();
        cache.putRawUrl(key, rawUrl);
        if (photo.user() != null && photo.user().links() != null) {
          cache.putAuthor(key, photo.user().name(), photo.user().links().html());
        }
      } catch (Exception e) {
        // fallback ป้องกันทั้งเพจพัง เช่น 401/เน็ตล่ม
        rawUrl = "https://placehold.co/800x600/png?text=Placeholder"; // รูป public ทั่วไป
      }
    }

    String src1x;
    try {
      src1x = unsplash.buildSrc(rawUrl, w, h, fit, 75, 1);
    } catch (Exception e) {
      // เผื่อ rawUrl ไม่ใช่ unsplash ครบๆ ให้มีแผนสำรองอีกชั้น
      src1x = "https://placehold.co/" + w + "x" + h + "/EEE/AAA?text=Image";
    }

    // แทนทั้งโทเคนเป็น URL จริง (ใช้ได้ทั้งใน src="[[...]]" และใน style=url('[[...]]'))
    m.appendReplacement(sb, Matcher.quoteReplacement(src1x));
  }
  m.appendTail(sb);
  return sb.toString();
}


  /** เติม srcset 1x/2x ให้ <img> ที่ยังไม่มี */
  public String ensureSrcset(String html) {
    Matcher m = IMG_NO_SRCSET.matcher(html);
    StringBuffer sb = new StringBuffer();

    while (m.find()) {
      String before = m.group(1);
      String src1x = m.group(2);   // มี query params อยู่แล้ว
      String after  = m.group(3);

      // แปลงเป็นรุ่น 2x โดยแก้ dpr = 2 และ q = 60
      String src2x = UriComponentsBuilder.fromUriString(src1x)
          .replaceQueryParam("dpr", 2)
          .replaceQueryParam("q", 60)
          .build(true).toUriString();

      String replaced = before +
          "src=\"" + src1x + "\" srcset=\"" + src1x + " 1x, " + src2x + " 2x\"" +
          after + ">";

      m.appendReplacement(sb, Matcher.quoteReplacement(replaced));
    }
    m.appendTail(sb);
    return sb.toString();
  }
}

