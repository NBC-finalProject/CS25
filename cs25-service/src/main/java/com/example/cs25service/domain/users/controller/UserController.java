package com.example.cs25service.domain.users.controller;

import com.example.cs25common.global.dto.ApiResponse;
import com.example.cs25service.domain.security.dto.AuthUser;
import com.example.cs25service.domain.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
//
//    /**
//     * FIXME: [임시] 로그인페이지 리다이렉트 페이지 컨트롤러
//     *
//     * @return 소셜로그인 페이지
//     */
//    @GetMapping("/")
//    public ResponseEntity<Void> redirectToLogin(HttpServletResponse response) throws IOException {
//        response.sendRedirect("/login");
//        return ResponseEntity.status(HttpStatus.FOUND).build();
//    }

    @PatchMapping("/users")
    public ApiResponse<Void> deleteUser(
        @AuthenticationPrincipal AuthUser authUser
    ) {
        userService.disableUser(authUser);
        return new ApiResponse<>(204, null);
    }
}
