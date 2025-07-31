package com.altradar.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimpleController {
    
    @GetMapping("/test")
    public String test() {
        return "AltRadar is running!";
    }
    
    @GetMapping("/api/status")
    public String status() {
        return "{\"status\": \"running\", \"message\": \"AltRadar application is running successfully\"}";
    }
} 