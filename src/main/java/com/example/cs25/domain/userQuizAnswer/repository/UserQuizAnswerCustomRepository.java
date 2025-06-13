package com.example.cs25.domain.userQuizAnswer.repository;

import com.example.cs25.domain.userQuizAnswer.dto.UserAnswerDto;
import com.example.cs25.domain.userQuizAnswer.entity.UserQuizAnswer;
import java.util.List;

public interface UserQuizAnswerCustomRepository{

    List<UserQuizAnswer> findByUserIdAndCategoryId(Long userId, Long categoryId);

    List<UserAnswerDto> findUserAnswerByQuizId(Long quizId);
}
