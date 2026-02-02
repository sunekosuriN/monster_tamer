package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GameController {
    @GetMapping("/")
    public String index() {
        // "index.html" ではなく "forward:/index.html" と書くか、
        // もしくはこのメソッド自体を消しても static なら自動で認識されます。
        return "forward:/index.html"; 
    }
}