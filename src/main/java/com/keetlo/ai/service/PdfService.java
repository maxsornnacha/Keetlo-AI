package com.keetlo.ai.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Entities;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
@Service
public class PdfService {

public byte[] htmlToPdfBytes(String html) {
  try (var out = new ByteArrayOutputStream()) {
    // 1) strip BOM and leading whitespace (fixes your 1:3 error)
    if (html != null && !html.isEmpty()) {
      html = html.replaceFirst("^[\\uFEFF\\s]+", "");
    }

    // 2) parse HTML5 and force XHTML output
    Document jsoupDoc = Jsoup.parse(html);
    jsoupDoc.outputSettings()
            .syntax(Document.OutputSettings.Syntax.xml)
            .escapeMode(Entities.EscapeMode.xhtml)
            .charset(StandardCharsets.UTF_8)
            .prettyPrint(false);

    // 3) ensure XHTML namespace
    var htmlEl = jsoupDoc.selectFirst("html");
    if (htmlEl != null) {
      htmlEl.attr("xmlns", "http://www.w3.org/1999/xhtml");
    }

    // 4) build W3C DOM and render
    String base = PdfService.class.getResource("/static/") != null
        ? PdfService.class.getResource("/static/").toExternalForm()
        : "file:.";

    org.w3c.dom.Document w3cDoc = new W3CDom().fromJsoup(jsoupDoc);

    var builder = new PdfRendererBuilder();
    builder.useFastMode();
    builder.withW3cDocument(w3cDoc, base);
    builder.toStream(out);
    builder.run();

    return out.toByteArray();
  } catch (Throwable e) {
    e.printStackTrace();
    throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
  }
}
}

