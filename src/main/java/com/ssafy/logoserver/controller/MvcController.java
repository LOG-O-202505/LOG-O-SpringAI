package com.ssafy.logoserver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MvcController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Home");
        return "home";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("title", "Login");
        return "login";
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("title", "Sign Up");
        return "signup";
    }

    @GetMapping("/mypage")
    public String mypage(Model model) {
        model.addAttribute("title", "My Page");
        return "mypage";
    }
}