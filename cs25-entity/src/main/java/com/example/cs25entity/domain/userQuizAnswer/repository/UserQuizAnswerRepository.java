package com.example.cs25entity.domain.userQuizAnswer.repository;

import com.example.cs25entity.domain.userQuizAnswer.entity.UserQuizAnswer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserQuizAnswerRepository extends JpaRepository<UserQuizAnswer, Long>,
    UserQuizAnswerCustomRepository {

    Optional<UserQuizAnswer> findFirstByQuizIdAndSubscriptionIdOrderByCreatedAtDesc(Long quizId,
        Long subscriptionId);

    List<UserQuizAnswer> findAllByQuizId(Long quizId);

    boolean existsByQuizIdAndSubscriptionId(Long quizId, Long subscriptionId);


    List<UserQuizAnswer> findAllByUserId(Long id);

    long countByQuizId(Long quizId);

    @Query("SELECT uqa FROM UserQuizAnswer uqa JOIN FETCH uqa.quiz WHERE uqa.id = :userQuizAnswerId")
    Optional<UserQuizAnswer> findByIdWithQuiz(Long userQuizAnswerId);
}
