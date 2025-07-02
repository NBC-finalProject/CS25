package com.example.cs25service.domain.quiz.service;


import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.repository.QuizCategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuizCategoryService {

    private final QuizCategoryRepository quizCategoryRepository;

    @Transactional(readOnly = true)
    public List<String> getParentQuizCategoryList() {
        return quizCategoryRepository.findByParentIdIsNull() //대분류만 찾아오도록 변경
            .stream()
            .map(QuizCategory::getCategoryType)
            .toList();
    }
}
