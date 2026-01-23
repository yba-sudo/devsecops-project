package com.devsecops.controller;

import com.devsecops.HelloController;
import com.devsecops.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HelloController.class)
public class HelloControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private MessageService messageService;
    
    @Test
    public void testHelloEndpoint() throws Exception {
        when(messageService.getWelcomeMessage()).thenReturn("Hello from DevSecOps Pipeline!");
        
        mockMvc.perform(get("/api/hello"))
               .andExpect(status().isOk())
               .andExpect(content().string("Hello from DevSecOps Pipeline!"));
    }
    
    @Test
    public void testHealthEndpoint() throws Exception {
        when(messageService.getHealthMessage()).thenReturn("Backend is healthy!");
        
        mockMvc.perform(get("/api/health"))
               .andExpect(status().isOk())
               .andExpect(content().string("Backend is healthy!"));
    }
    
    @Test
    public void testGreetEndpoint() throws Exception {
        when(messageService.getCustomMessage("John")).thenReturn("Hello, John!");
        
        mockMvc.perform(get("/api/greet").param("name", "John"))
               .andExpect(status().isOk())
               .andExpect(content().string("Hello, John!"));
    }
}
