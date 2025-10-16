package com.keetlo.ai.service;

import com.keetlo.ai.model.Order;
import com.keetlo.ai.util.SimpleTemplateRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class OrderDocumentService {

    private final SimpleTemplateRenderer renderer;
    private final PdfService pdfService;                  // your existing class
    private final EmailService emailService;                // where sendWithAttachment lives

    @Value("${company.name:KEETLO}")    private String companyName;
    @Value("${company.website:https://www.keetlo.com}") private String companyWebsite;
    @Value("${mail.support:keetlo.ai@gmail.com}")        private String supportEmail;

    public OrderDocumentService(SimpleTemplateRenderer renderer,
                                PdfService pdfService,
                                EmailService emailService) {
        this.renderer = renderer;
        this.pdfService = pdfService;
        this.emailService = emailService;
    }

    private Map<String, String> modelFor(Order order) {
        var dfDate      = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        var dfDateTime  = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        Map<String, String> model = new LinkedHashMap<>();
        model.put("company_name", companyName);
        model.put("company_website", companyWebsite);
        model.put("support_email", supportEmail);

        model.put("receipt_id", order.getReceiptId());
        model.put("created_at", order.getCreatedAt() != null ? dfDateTime.format(order.getCreatedAt()) : "");
        model.put("status", order.getStatus() != null ? order.getStatus().name() : "PENDING");

        model.put("customer_name", order.getName());
        model.put("customer_email", order.getEmail());

        model.put("plan_name", order.getPlanName());
        model.put("plan_description", order.getPlanDescription());
        model.put("start_date", order.getStartDate() != null ? dfDate.format(order.getStartDate()) : "");
        model.put("end_date", order.getEndDate() != null ? dfDate.format(order.getEndDate()) : "");

        var amount = order.getAmount() != null ? order.getAmount().toPlainString() : "0.00";
        model.put("unit_price", amount);
        model.put("amount", amount);
        model.put("currency", order.getCurrency() != null ? order.getCurrency() : "");
        model.put("currency_symbol", order.getCurrencySymbol() != null ? order.getCurrencySymbol() : "");

        model.put("now", dfDateTime.format(java.time.LocalDateTime.now()));
        return model;
    }

    public byte[] buildReceiptPdf(Order order) {
        String html = renderer.render("templates/order-receipt.html", modelFor(order));
        return pdfService.htmlToPdfBytes(html);
    }

    public void emailReceiptPdf(Order order) {
        byte[] pdf = buildReceiptPdf(order);
        String subject = "Receipt " + order.getReceiptId();
        String bodyHtml = """
            <p>Hi %s,</p>
            <p>Your payment was successful. Your receipt <strong>%s</strong> is attached.</p>
            <p>— %s</p>
            """.formatted(order.getName(), order.getReceiptId(), companyName);

        emailService.sendWithAttachment(
            order.getEmail(),
            subject,
            bodyHtml,
            pdf,
            "receipt-" + order.getReceiptId() + ".pdf",
            "application/pdf"
        );
    }
}
