package com.devsecops;

import com.devsecops.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {
    
    @Autowired
    private MessageService messageService;
    
    @GetMapping("/hello")
    public String sayHello() {
        return messageService.getWelcomeMessage();
    }
    
    @GetMapping("/health")
    public String healthCheck() {
        return messageService.getHealthMessage();
    }
    
    @GetMapping("/greet")
    public String greetUser(@RequestParam(defaultValue = "Developer") String name) {
        return messageService.getCustomMessage(name);
    }
}
