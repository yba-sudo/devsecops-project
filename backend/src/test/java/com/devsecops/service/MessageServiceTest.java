package com.devsecops.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {
    
    @InjectMocks
    private MessageService messageService;
    
    @Test
    public void testGetWelcomeMessage() {
        String result = messageService.getWelcomeMessage();
        assertEquals("Hello from DevSecOps Pipeline!", result);
    }
    
    @Test
    public void testGetHealthMessage() {
        String result = messageService.getHealthMessage();
        assertEquals("Backend is healthy!", result);
    }
    
    @Test
    public void testGetCustomMessage() {
        String result = messageService.getCustomMessage("John");
        assertEquals("Hello, John!", result);
    }
}
