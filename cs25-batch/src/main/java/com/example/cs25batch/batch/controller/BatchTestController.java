package com.example.cs25batch.batch.controller;

import com.example.cs25batch.batch.service.BatchService;
import com.example.cs25common.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BatchTestController {

    private final BatchService batchService;

    @PostMapping("/emails/activeProducerJob")
    public ApiResponse<String> activeProducerJob(
    ) {
        batchService.activeProducerJob();
        return new ApiResponse<>(200, "스프링 배치 - 큐에 넣기 성공");
    }

    @PostMapping("/emails/activeConsumerJob")
    public ApiResponse<String> activeConsumerJob(
    ) {
        batchService.activeConsumerJob();
        return new ApiResponse<>(200, "스프링 배치 - 문제 발송 성공");
    }
}
