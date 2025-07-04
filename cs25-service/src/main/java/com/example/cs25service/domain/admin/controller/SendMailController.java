package com.example.cs25service.domain.admin.controller;

import com.example.cs25common.global.dto.ApiResponse;
import com.example.cs25entity.domain.quiz.enums.QuizFormatType;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/admin")
public class SendMailController {

    @PostMapping("/mail-logs/retry")
    public ApiResponse<String> (

    ) {

    }
}
