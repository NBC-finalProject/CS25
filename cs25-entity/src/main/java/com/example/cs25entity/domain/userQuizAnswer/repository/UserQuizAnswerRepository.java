package com.example.cs25entity.domain.userQuizAnswer.repository;

import com.example.cs25entity.domain.userQuizAnswer.entity.UserQuizAnswer;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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


    Page<UserQuizAnswer> findAllByUserId(Long id, Pageable pageable);

    long countByQuizId(Long quizId);

    @Query("SELECT a FROM UserQuizAnswer a JOIN FETCH a.quiz LEFT JOIN FETCH a.user WHERE a.id = :id")
    Optional<UserQuizAnswer> findWithQuizAndUserById(@Param("id") Long id);
}
