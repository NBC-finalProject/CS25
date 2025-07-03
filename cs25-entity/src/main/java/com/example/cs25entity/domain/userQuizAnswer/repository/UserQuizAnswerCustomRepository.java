package com.example.cs25entity.domain.userQuizAnswer.repository;

import com.example.cs25entity.domain.userQuizAnswer.dto.UserAnswerDto;
import com.example.cs25entity.domain.userQuizAnswer.entity.UserQuizAnswer;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserQuizAnswerCustomRepository {

    List<UserAnswerDto> findUserAnswerByQuizId(Long quizId);

    List<UserQuizAnswer> findByUserIdAndQuizCategoryId(Long userId, Long quizCategoryId);

    List<UserQuizAnswer> findBySubscriptionIdAndQuizCategoryId(Long subscriptionId,
        Long quizCategoryId);

    Set<Long> findRecentSolvedCategoryIds(Long userId, Long parentCategoryId, LocalDate afterDate);

    UserQuizAnswer findUserQuizAnswerBySerialIds(String quizId, String subscriptionId);
}
