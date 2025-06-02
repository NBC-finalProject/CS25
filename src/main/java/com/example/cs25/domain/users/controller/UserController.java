package com.example.cs25.domain.users.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
	/**
	 * FIXME: [임시] 로그인페이지 리다이렉트 페이지 컨트롤러
	 * @return 소셜로그인 페이지
	 */
	@GetMapping("/")
	public String redirectToLogin() {
		return "redirect:/login";
	}
}
