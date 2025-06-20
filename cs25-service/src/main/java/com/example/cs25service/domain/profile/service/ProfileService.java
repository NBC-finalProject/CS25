package com.example.cs25service.domain.profile.service;

import com.example.cs25entity.domain.user.entity.User;
import com.example.cs25entity.domain.user.exception.UserException;
import com.example.cs25entity.domain.user.exception.UserExceptionCode;
import com.example.cs25entity.domain.user.repository.UserRepository;
import com.example.cs25entity.domain.userQuizAnswer.entity.UserQuizAnswer;
import com.example.cs25entity.domain.userQuizAnswer.exception.UserQuizAnswerException;
import com.example.cs25entity.domain.userQuizAnswer.exception.UserQuizAnswerExceptionCode;
import com.example.cs25entity.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import com.example.cs25service.domain.profile.dto.ProfileResponseDto;
import com.example.cs25service.domain.profile.dto.WrongQuizResponseDto;
import com.example.cs25service.domain.quiz.dto.QuizResponseDto;
import com.example.cs25service.domain.security.dto.AuthUser;
import com.example.cs25service.domain.userQuizAnswer.dto.UserQuizAnswerRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserQuizAnswerRepository userQuizAnswerRepository;

    // 유저 틀린 문제 다시보기
    public ProfileResponseDto getWrongQuiz(AuthUser authUser) {

        List<WrongQuizResponseDto> wrongQuizList = userQuizAnswerRepository
                // 유저 아이디로 내가 푼 문제 조회
                .findAllByUserId(authUser.getId()).stream()
                .filter(answer -> !answer.getIsCorrect()) // 틀린 문제
                .map(answer -> new WrongQuizResponseDto(
                        answer.getQuiz().getQuestion(),
                        answer.getUserAnswer(),
                        answer.getQuiz().getAnswer(),
                        answer.getQuiz().getCommentary()
                ))
                .collect(Collectors.toList());

        return new ProfileResponseDto(authUser.getId(), wrongQuizList);
    }
}
