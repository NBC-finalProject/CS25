package com.example.cs25entity.domain.quiz.repository;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.quiz.exception.QuizException;
import com.example.cs25entity.domain.quiz.exception.QuizExceptionCode;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long>, QuizCustomRepository {

    List<Quiz> findAllByCategoryIdIn(Collection<Long> categoryIds);

    @Query("SELECT q FROM Quiz q ORDER BY q.createdAt DESC")
    Page<Quiz> findAllOrderByCreatedAtDesc(Pageable pageable);

    Optional<Quiz> findBySerialId(String quizId);

    default Quiz findBySerialIdOrElseThrow(String quizId){
        return findBySerialId(quizId)
            .orElseThrow(()-> new QuizException(QuizExceptionCode.NOT_FOUND_ERROR));
    }

    Optional<Quiz> findById(Long id);

    default Quiz findByIdOrElseThrow(Long id) {
        return findById(id)
            .orElseThrow(() -> new QuizException(QuizExceptionCode.NO_QUIZ_EXISTS_ERROR));
    }

    void deleteAllByIdIn(Collection<Long> ids);
}
