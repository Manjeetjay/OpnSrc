package com.osc.backend.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin("*")
public class Health {

    @GetMapping("/ping")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Pong");
    }
}
