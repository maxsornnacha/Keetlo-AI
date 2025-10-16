package com.keetlo.ai.service;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

public class EmailService {
    private JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public String loadEmailTemplate(String fileName) throws Exception {
    byte[] bytes = Files.readAllBytes(
        Paths.get(getClass().getClassLoader()
                  .getResource("templates/"+fileName)
                  .toURI())
    );
    return new String(bytes, StandardCharsets.UTF_8);
    }

    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    public void sendWithAttachment(
            String to, String subject, String htmlBody,
            byte[] attachment, String fileName, String contentType
    ) {
        try {
            var mm = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mm, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // HTML body (email body, separate from PDF)
            helper.addAttachment(fileName, new ByteArrayResource(attachment), contentType);
            mailSender.send(mm);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
}
