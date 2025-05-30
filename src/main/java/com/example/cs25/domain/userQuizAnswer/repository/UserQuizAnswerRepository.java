package com.example.cs25.domain.userQuizAnswer.repository;

import com.example.cs25.domain.userQuizAnswer.entity.UserQuizAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserQuizAnswerRepository extends JpaRepository<UserQuizAnswer, Long> {
    Optional<UserQuizAnswer> findFirstByQuizIdOrderByCreatedAtDesc(Long quizId);
}
