package com.knds.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ping {
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}
