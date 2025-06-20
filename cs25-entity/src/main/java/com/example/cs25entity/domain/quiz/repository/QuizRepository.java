package com.example.cs25entity.domain.quiz.repository;

import com.example.cs25entity.domain.quiz.entity.Quiz;

import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findAllByCategoryId(Long categoryId);

    List<Quiz> findAllByCategoryIdIn(Collection<Long> categoryIds);

    @Query("SELECT q FROM Quiz q ORDER BY q.createdAt DESC")
    Page<Quiz> findAllOrderByCreatedAtDesc(Pageable pageable);

}
