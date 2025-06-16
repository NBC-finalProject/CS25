package com.example.cs25.batch.controller;

import com.example.cs25.batch.service.BatchService;
import com.example.cs25.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BatchTestController {

    private final BatchService batchService;

    @PostMapping("/emails/sendTodayQuizzes")
    public ApiResponse<String> sendTodayQuizzes(
    ){
        batchService.activeBatch();
        return new ApiResponse<>(200, "스프링 배치 - 문제 발송 성공");
    }
}
