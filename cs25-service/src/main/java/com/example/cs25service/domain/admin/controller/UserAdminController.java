package com.example.cs25service.domain.admin.controller;

import com.example.cs25common.global.dto.ApiResponse;
import com.example.cs25service.domain.admin.dto.response.UserDetailResponseDto;
import com.example.cs25service.domain.admin.dto.response.UserPageResponseDto;
import com.example.cs25service.domain.admin.service.UserAdminService;
import com.example.cs25service.domain.subscription.dto.SubscriptionRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserAdminService userAdminService;
    
    //GET	관리자 사용자(회원) 목록 조회	/admin/users
    @GetMapping
    public ApiResponse<Page<UserPageResponseDto>> getUserLists(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "30") int size
    ) {
        return new ApiResponse<>(200, userAdminService.getAdminUsers(page, size));
    }

    //GET	관리자 사용자(회원) 상세 조회	/admin/users/{userId}
    @GetMapping("/{userId}")
    public ApiResponse<UserDetailResponseDto> getUserDetail(
        @Positive @PathVariable(name = "userId") Long userId
    ) {
        return new ApiResponse<>(200, userAdminService.getAdminUserDetail(userId));
    }

    //DELETE	관리자 사용자(회원) 탈퇴	/admin/users/{userId}
    @DeleteMapping("/{userId}")
    public ApiResponse<Void> disableUser(
        @Positive @PathVariable(name = "userId") Long userId
    ) {
        userAdminService.disableUser(userId);

        return new ApiResponse<>(204);
    }

    //PATCH	관리자 사용자(회원) 구독 상태 변경	/admin/users/{userId}/subscriptions
    @PatchMapping("/{userId}")
    public ApiResponse<String> updateAdminSubscription(
        @Positive @PathVariable(name = "userId") Long userId,
        @RequestBody @Valid SubscriptionRequestDto request
    ) {
        userAdminService.updateSubscription(userId, request);
        return new ApiResponse<>(200, "구독 정보 수정 성공");
    }

    //DELETE	관리자 사용자(회원) 구독 취소 	/admin/users/{userId}/subscriptions
    @DeleteMapping("/{userId}")
    public ApiResponse<Void> cancelSubscription(
        @Positive @PathVariable(name = "userId") Long userId
    ) {
        userAdminService.cancelSubscription(userId);

        return new ApiResponse<>(204);
    }
}
