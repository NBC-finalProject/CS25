package com.example.cs25service.domain.quiz.controller;

import com.example.cs25common.global.dto.ApiResponse;
import com.example.cs25entity.domain.quiz.enums.QuizFormatType;
import com.example.cs25service.domain.quiz.dto.QuizResponseDto;
import com.example.cs25entity.domain.quiz.dto.QuizSearchDto;
import com.example.cs25service.domain.quiz.service.QuizService;
import com.example.cs25service.domain.security.dto.AuthUser;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/quizzes")
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/upload")
    public ApiResponse<String> uploadQuizByJsonFile(
        @RequestParam("file") MultipartFile file,
        @RequestParam("categoryType") String categoryType,
        @RequestParam("formatType") QuizFormatType formatType,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        if (file.isEmpty()) {
            return new ApiResponse<>(400, "파일이 비어있습니다.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals(MediaType.APPLICATION_JSON_VALUE)) {
            return new ApiResponse<>(400, "JSON 파일만 업로드 가능합니다.");
        }

        quizService.uploadQuizJson(file, categoryType, formatType);
        return new ApiResponse<>(200, "문제 등록 성공");
    }

    //퀴즈 목록 조회
    @GetMapping
    public ApiResponse<Page<QuizResponseDto>> getQuizzes(
        @RequestBody QuizSearchDto condition,
        @PageableDefault(size = 20, sort = "category", direction = Direction.ASC) Pageable pageable,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        return new ApiResponse<>(200, quizService.getQuizzes(condition, pageable));
    }

    //단일 퀴즈 조회
    @GetMapping("/{quizId}")
    public ApiResponse<QuizResponseDto> getQuiz(
        @PathVariable @NotNull Long quizId,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        return new ApiResponse<>(200, quizService.getQuiz(quizId));
    }

    @DeleteMapping
    public ApiResponse<String> deleteQuizzes(
        @RequestBody List<Long> quizIds,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        quizService.deleteQuizzes(quizIds);
        return new ApiResponse<>(200, "문제 삭제 완료");
    }

}
