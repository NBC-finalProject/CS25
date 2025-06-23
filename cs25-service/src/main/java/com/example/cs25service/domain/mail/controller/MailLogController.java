package com.example.cs25service.domain.mail.controller;

import com.example.cs25common.global.dto.ApiResponse;
import com.example.cs25entity.domain.mail.dto.MailLogSearchDto;
import com.example.cs25service.domain.mail.dto.MailLogDetailResponse;
import com.example.cs25service.domain.mail.dto.MailLogResponse;
import com.example.cs25service.domain.mail.service.MailLogService;
import com.example.cs25service.domain.security.dto.AuthUser;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mail-logs")
@RequiredArgsConstructor
public class MailLogController {
    private final MailLogService mailLogService;

    @GetMapping
    public ApiResponse<Page<MailLogResponse>> getMailLogs(
        @RequestBody MailLogSearchDto condition,
        @PageableDefault(size = 20, sort = "sendDate", direction = Direction.DESC) Pageable pageable,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        Page<MailLogResponse> results =  mailLogService.getMailLogs(authUser, condition, pageable);
        return new ApiResponse<>(200, results);
    }

    @GetMapping("/{mailLogId}")
    public ApiResponse<MailLogDetailResponse> getMailLog(
            @PathVariable @NotNull Long mailLogId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        MailLogDetailResponse result =  mailLogService.getMailLog(authUser, mailLogId);
        return new ApiResponse<>(200, result);
    }

    @DeleteMapping
    public ApiResponse<String> deleteMailLogs(
        @RequestBody List<Long> mailLogIds,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        mailLogService.deleteMailLogs(authUser, mailLogIds);
        return new ApiResponse<>(200, "MailLog 삭제 완료");
    }
}
