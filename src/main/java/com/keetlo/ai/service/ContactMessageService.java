package com.keetlo.ai.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.keetlo.ai.model.ContactMessage;

@Service
public class ContactMessageService {
    private final JdbcTemplate database;

    public ContactMessageService(JdbcTemplate database) {
        this.database = database;
    }
    

     public Boolean createMessage(ContactMessage request) {
      try{
        ContactMessage contactMessage = new ContactMessage();

        String sql = """
            INSERT INTO contact_messages
            (contact_message_id, name, subject, email, message)
            VALUES (?, ?, ?, ?, ?)
        """;
        database.update(sql,
                contactMessage.createContactMessageId(),
                request.getName(),
                request.getSubject(),
                request.getEmail(),
                request.getMessage());

        return true;
      } catch (Exception e){
        System.out.println("Error Creating contact message: " + e.getMessage());
        return false;
      }
    }
}
