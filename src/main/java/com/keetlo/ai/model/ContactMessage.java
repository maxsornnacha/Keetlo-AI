package com.keetlo.ai.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class ContactMessage {
    private String contactMessageId;
    private String name;
    private String subject;
    private String email;
    private String message;
    private LocalDateTime createdAt;

    // Getter and Setter for contactMessageId
    public String getContactMessageId() {
        return contactMessageId;
    }

    public void setContactMessageId(String contactMessageId) {
        this.contactMessageId = contactMessageId;
    }

    // Getter and Setter for name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter and Setter for email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    // Getter and Setter for message
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // Getter and Setter for createdAt
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

        //Helper method
    public String createContactMessageId() {
        return UUID.randomUUID().toString();
    }
}
