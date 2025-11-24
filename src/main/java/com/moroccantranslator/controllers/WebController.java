package com.moroccantranslator.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// Can be seen as a client that consumes this API (specifically via the js script)
@Controller
public class WebController {

    @GetMapping("/")
    public String home() {
        return "index";
    }
}
