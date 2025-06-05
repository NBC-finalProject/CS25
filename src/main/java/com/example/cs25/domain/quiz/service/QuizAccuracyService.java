package com.example.cs25.domain.quiz.service;

import com.example.cs25.domain.quiz.entity.Quiz;
import com.example.cs25.domain.quiz.entity.QuizAccuracy;
import com.example.cs25.domain.quiz.repository.QuizAccuracyRedisRepository;
import com.example.cs25.domain.quiz.repository.QuizRepository;
import com.example.cs25.domain.userQuizAnswer.entity.UserQuizAnswer;
import com.example.cs25.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizAccuracyService {

    private final QuizRepository quizRepository;
    private final QuizAccuracyRedisRepository quizAccuracyRedisRepository;
    private final UserQuizAnswerRepository userQuizAnswerRepository;

    public void calculateAndCacheAllQuizAccuracies() {
        List<Quiz> quizzes = quizRepository.findAll();

        List<QuizAccuracy> accuracyList = new ArrayList<>();
        for (Quiz quiz : quizzes) {

            List<UserQuizAnswer> answers = userQuizAnswerRepository.findAllByQuizId(quiz.getId());
            long total = answers.size();
            long correct = answers.stream().filter(UserQuizAnswer::getIsCorrect).count();
            double accuracy = total == 0 ? 0.0 : ((double) correct / total);

            QuizAccuracy qa = QuizAccuracy.builder()
                .id("quiz:" + quiz.getId())
                .quizId(quiz.getId())
                .categoryId(quiz.getCategory().getId())
                .accuracy(accuracy)
                .build();

            accuracyList.add(qa);

            log.info("✔ [quiz] 추가 : {}", quiz.getId());
        }
        log.info("총 {}개의 정답률 캐싱 완료", accuracyList.size());
        quizAccuracyRedisRepository.saveAll(accuracyList);
    }

}
