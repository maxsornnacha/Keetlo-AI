package com.keetlo.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

import com.keetlo.ai.service.EmailService;

@Configuration
public class EmailConfig {
    @Bean
    public EmailService emailService(JavaMailSender mailSender) {
        return new EmailService(mailSender);
    }
    
}
