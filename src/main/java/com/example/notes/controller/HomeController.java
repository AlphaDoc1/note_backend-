package com.example.notes.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        // If your index.html is served as static content (placed in src/main/resources/static)
        return "redirect:/index.html";
    }
}
