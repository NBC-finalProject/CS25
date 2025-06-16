package com.example.cs25service.domain.users.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginPageController {

    @GetMapping("/")
    public String showLoginPage() {
        return "login"; // templates/login.html 렌더링
    }

    @GetMapping("/login")
    public String showLoginPageAlias() {
        return "login";
    }
}
