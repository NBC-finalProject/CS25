package com.example.cs25service.domain.quiz.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.cs25entity.domain.quiz.repository.QuizCategoryRepository;
import com.example.cs25entity.domain.quiz.repository.QuizRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.xml.validation.Validator;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @InjectMocks
    private QuizService quizService;

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private Validator validator;
    @Mock
    private QuizRepository quizRepository;
    @Mock
    private QuizCategoryRepository quizCategoryRepository;


}