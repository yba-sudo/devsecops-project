package com.devsecops.service;

import org.springframework.stereotype.Service;

@Service
public class MessageService {
    
    public String getWelcomeMessage() {
        return "Hello from DevSecOps Pipeline!";
    }
    
    public String getHealthMessage() {
        return "Backend is healthy!";
    }
    
    public String getCustomMessage(String name) {
        return "Hello, " + name + "!";
    }
}
