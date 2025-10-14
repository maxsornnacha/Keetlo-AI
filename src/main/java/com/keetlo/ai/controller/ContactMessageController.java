package com.keetlo.ai.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.keetlo.ai.model.ContactMessage;
import com.keetlo.ai.service.ContactMessageService;

@RestController
@RequestMapping("/contact-message")
public class ContactMessageController {

    private final ContactMessageService service;

    public ContactMessageController(ContactMessageService service) {
        this.service = service;
    }


    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody ContactMessage request) {
        Map<String, Object> response = new HashMap<>();
        try{
        Boolean isSuccess = service.createMessage(request);
        if(!isSuccess){
            response.put("message", "Server interval error");
            return ResponseEntity.status(400).body(response);
        }
        response.put("message", "Message got craeted successfully!");
        return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            response.put("message", "Server interval error");
            return ResponseEntity.status(500).body(response);
        }
    }
    
}
