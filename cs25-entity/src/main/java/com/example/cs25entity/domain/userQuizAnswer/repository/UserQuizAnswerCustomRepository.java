package com.example.cs25entity.domain.userQuizAnswer.repository;

import com.example.cs25entity.domain.userQuizAnswer.dto.UserAnswerDto;
import com.example.cs25entity.domain.userQuizAnswer.entity.UserQuizAnswer;
import java.util.List;

public interface UserQuizAnswerCustomRepository {

    List<UserAnswerDto> findUserAnswerByQuizId(Long quizId);

    List<UserQuizAnswer> findByUserIdAndQuizCategoryId(Long userId, Long quizCategoryId);

    Double getCorrectRate(Long subscriptionId, Long quizCategoryId);

    UserQuizAnswer findUserQuizAnswerBySerialIds(String quizId, String subscriptionId);
}
