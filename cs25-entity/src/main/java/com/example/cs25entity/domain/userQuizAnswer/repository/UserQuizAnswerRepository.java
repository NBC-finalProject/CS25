package com.example.cs25entity.domain.userQuizAnswer.repository;

import com.example.cs25entity.domain.quiz.exception.QuizException;
import com.example.cs25entity.domain.userQuizAnswer.entity.UserQuizAnswer;
import com.example.cs25entity.domain.userQuizAnswer.exception.UserQuizAnswerException;
import com.example.cs25entity.domain.userQuizAnswer.exception.UserQuizAnswerExceptionCode;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserQuizAnswerRepository extends JpaRepository<UserQuizAnswer, Long>,
    UserQuizAnswerCustomRepository {

    default UserQuizAnswer findByIdOrElseThrow(Long id) {
        return findById(id)
            .orElseThrow(() -> new UserQuizAnswerException(UserQuizAnswerExceptionCode.NOT_FOUND_ANSWER));
    }

    List<UserQuizAnswer> findAllByQuizId(Long quizId);

    boolean existsByQuizIdAndSubscriptionId(Long quizId, Long subscriptionId);

    long countByQuizId(Long quizId);

    @Query("SELECT a FROM UserQuizAnswer a JOIN FETCH a.quiz LEFT JOIN FETCH a.user WHERE a.id = :id")
    Optional<UserQuizAnswer> findWithQuizAndUserById(@Param("id") Long id);

    default UserQuizAnswer findWithQuizAndUserByIdOrElseThrow(Long id) {
        return findWithQuizAndUserById(id)
            .orElseThrow(() -> new UserQuizAnswerException(UserQuizAnswerExceptionCode.NOT_FOUND_ANSWER));
    }

    @Query("SELECT a FROM UserQuizAnswer a WHERE a.user.id = :userId AND a.isCorrect = false")
    Page<UserQuizAnswer> findAllByUserIdAndIsCorrectFalse(Long userId, Pageable pageable);
}
