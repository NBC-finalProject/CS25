package com.example.cs25service.domain.ai.test;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.quiz.enums.QuizFormatType;
import com.example.cs25entity.domain.quiz.enums.QuizLevel;
import com.example.cs25entity.domain.quiz.repository.QuizRepository;
import com.example.cs25entity.domain.user.entity.User;
import com.example.cs25entity.domain.user.repository.UserRepository;
import com.example.cs25entity.domain.userQuizAnswer.entity.UserQuizAnswer;
import com.example.cs25entity.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("loadtest") // loadtest 프로파일일 때만 실행됨
@RequiredArgsConstructor
public class TestDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final UserQuizAnswerRepository answerRepository;

    @Override
    @Transactional
    public void run(String... args) {
           // 기존 테스트 데이터가 있는지 확인
        if (userRepository.existsByEmail("loadtest@test.com")) {
            return; // 이미 데이터가 존재하면 종료
        }

            // 1. 테스트 유저 생성
        User user = User.builder()
            .email("loadtest@test.com")
            .score(0.0)
            .build();
        userRepository.save(user);

        // 2. 테스트 퀴즈 생성
        Quiz quiz = Quiz.builder()
            .type(QuizFormatType.SUBJECTIVE)
            .question("HTTP란 무엇인가?")
            .answer("HyperText Transfer Protocol")
            .commentary("HTTP는 웹 통신의 기반 프로토콜입니다.")
            .choice(null) // 주관식은 보기 없음
            .category(null) // 필요시 지정
            .level(QuizLevel.EASY)
            .build();
        quizRepository.save(quiz);

        // 3. UserQuizAnswer 1000개 bulk insert
        List<UserQuizAnswer> answers = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            answers.add(UserQuizAnswer.builder()
                .user(user)
                .quiz(quiz)
                .userAnswer("HTTP는 ...") // 필드 이름 주의!
                .aiFeedback(null)
                .isCorrect(null)
                .subscription(null) // 필요시 연결
                .build());
        }
        answerRepository.saveAll(answers);
    }
}
