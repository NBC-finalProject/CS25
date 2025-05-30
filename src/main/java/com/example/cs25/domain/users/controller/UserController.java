package com.example.cs25.domain.users.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @GetMapping("/home")
    public String home() {
        return "로그인 성공!"; // 또는 home.html 뷰 페이지
    }
}
