package com.example.cs25.domain.userQuizAnswer.repository;

import com.example.cs25.domain.userQuizAnswer.entity.UserQuizAnswer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserQuizAnswerRepository extends JpaRepository<UserQuizAnswer, Long>,
    UserQuizAnswerCustomRepository {

    Optional<UserQuizAnswer> findFirstByQuizIdAndSubscriptionIdOrderByCreatedAtDesc(Long quizId,
        Long subscriptionId);

}
