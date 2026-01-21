package com.devsecops.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {
    
    @GetMapping("/hello")
    public String sayHello() {
        return "Hello from DevSecOps Pipeline!";
    }
    
    @GetMapping("/health")
    public String healthCheck() {
        return "Backend is healthy!";
    }
}
