package com.example.cs25service.domain.mail.controller;

import com.example.cs25common.global.dto.ApiResponse;
import com.example.cs25entity.domain.mail.dto.MailLogSearchDto;
import com.example.cs25service.domain.mail.dto.MailLogDetailResponse;
import com.example.cs25service.domain.mail.dto.MailLogResponse;
import com.example.cs25service.domain.mail.service.MailLogService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
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
    public Page<MailLogResponse> getMailLogs(
        @RequestBody MailLogSearchDto condition,
        @PageableDefault(size = 20, sort = "sendDate", direction = Direction.DESC) Pageable pageable
    ) {
        return mailLogService.getMailLogs(condition, pageable);
    }

    @GetMapping("/{mailLogId}")
    public MailLogDetailResponse getMailLog(@PathVariable Long mailLogId) {
        return mailLogService.getMailLog(mailLogId);
    }

    @DeleteMapping
    public ApiResponse<String> deleteMailLogs(@RequestBody List<Long> mailLogIdids) {
        mailLogService.deleteMailLogs(mailLogIdids);
        return new ApiResponse<>(200, "MailLog 삭제 완료");
    }
}
