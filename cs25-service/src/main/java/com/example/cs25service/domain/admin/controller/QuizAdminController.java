package com.example.cs25service.domain.admin.controller;

import com.example.cs25common.global.dto.ApiResponse;
import com.example.cs25service.domain.admin.dto.request.QuizCreateRequestDto;
import com.example.cs25service.domain.admin.dto.request.QuizUpdateRequestDto;
import com.example.cs25service.domain.admin.dto.response.QuizDetailDto;
import com.example.cs25service.domain.admin.service.QuizAdminService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/quizzes")
public class QuizAdminController {

    private final QuizAdminService quizAdminService;

    //GET	관리자 문제 목록 조회 (기본값: 비추천 오름차순)	/admin/quizzes
    @GetMapping
    public ApiResponse<Page<QuizDetailDto>> getQuizDetails(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "30") int size
    ) {
        return new ApiResponse<>(200, quizAdminService.getAdminQuizDetails(page, size));
    }

    //GET	관리자 문제  상세 조회	/admin/quizzes/{quizId}
    @GetMapping("/{quizId}")
    public ApiResponse<QuizDetailDto> getQuizDetails(
        @Positive @PathVariable(name = "quizId") Long quizId
    ) {
        return new ApiResponse<>(200, quizAdminService.getAdminQuizDetail(quizId));
    }


    //POST	관리자 문제 등록	/admin/quizzes
    @PostMapping
    public ApiResponse<Long> createQuiz(
        @RequestBody QuizCreateRequestDto requestDto
    ) {
        return new ApiResponse<>(201, quizAdminService.createQuiz(requestDto));
    }

    //PATCH	관리자 문제 수정	/admin/quizzes/{quizId}
    @PatchMapping("{quizId}")
    public ApiResponse<QuizDetailDto> updateQuiz(
        @Positive @PathVariable(name = "quizId") Long quizId,
        @RequestBody QuizUpdateRequestDto requestDto
    ) {
        return new ApiResponse<>(200, quizAdminService.updateQuiz(quizId, requestDto));
    }

    //DELETE	관리자 문제 삭제	/admin/quizzes/{quizId}
    @DeleteMapping("{quizId}")
    public ApiResponse<Void> deleteQuiz(
        @Positive @PathVariable(name = "quizId") Long quizId
    ) {
        quizAdminService.deleteQuiz(quizId);

        return new ApiResponse<>(204);
    }
}
