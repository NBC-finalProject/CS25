package com.example.cs25batch.batch.service;

import com.example.cs25batch.Cs25BatchApplication;
import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.enums.QuizFormatType;
import com.example.cs25entity.domain.quiz.enums.QuizLevel;
import com.example.cs25entity.domain.quiz.repository.QuizCategoryRepository;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.datafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest(classes = Cs25BatchApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled
class TodayQuizServiceInsertTest {

    @Autowired
    QuizCategoryRepository quizCategoryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    QuizCategory parent;
    List<QuizCategory> categories = new ArrayList<>();

    private long startTime;

    @BeforeEach
    void beforeEach(TestInfo testInfo) {
        startTime = System.currentTimeMillis();
        System.out.println("!!!!!시작: " + testInfo.getDisplayName());
    }

    @AfterEach
    void afterEach(TestInfo testInfo) {
        long duration = System.currentTimeMillis() - startTime;
        System.out.print("@@@@ 종료: " + testInfo.getDisplayName());
        System.out.println("  (소요 시간: " + duration + " ms)");
    }


    @Test
    @Order(1)
    @DisplayName("테스트용 카테고리 5개 넣기")
        //   "테스트용 카테고리 5개 넣기")
    void insertQuizCategories() {

        parent = QuizCategory.builder()
            .categoryType("TEST")
            .parent(null)
            .build();

        quizCategoryRepository.save(parent);

        for (int i = 1; i <= 5; i++) {
            QuizCategory sub = QuizCategory.builder()
                .categoryType("Sub" + i)
                .parent(parent)
                .build();
            quizCategoryRepository.save(sub);
            categories.add(sub);
        }
    }

    @Test
    @Order(4)
    @DisplayName("퀴즈 답변 100만개 넣기")
    void insertUserQuizAnswersTest() {
        List<Long> subscriptionIds = jdbcTemplate.queryForList(
            "SELECT id FROM subscription", Long.class);

        List<Long> quizIds = jdbcTemplate.queryForList(
            "SELECT id FROM quiz", Long.class);

        insertUserQuizAnswers(subscriptionIds, quizIds);
    }

    void insertUserQuizAnswers(List<Long> subscriptionIds, List<Long> quizIds) {
        Random random = new Random();
        Faker faker = new Faker();

        int batchSize = 5000;
        List<Object[]> batch = new ArrayList<>();

        for (Long subId : subscriptionIds) {
            int count = random.nextInt(100) + 1; // 1~100
            Set<Long> sample = getRandomSample(quizIds, count);

            for (Long quizId : sample) {
                batch.add(new Object[]{
                    Timestamp.valueOf(LocalDateTime.now().minusDays(random.nextInt(30))),
                    faker.lorem().sentence(), //답변
                    random.nextBoolean(), // 정답 여부
                    quizId,
                    subId
                });

                // 배치 실행
                if (batch.size() >= batchSize) {
                    jdbcTemplate.batchUpdate(
                        "INSERT INTO user_quiz_answers (created_at, user_answer, is_correct, quiz_id, subscription_id) "
                            +
                            "VALUES (?, ?, ?, ?, ?)",
                        batch
                    );
                    batch.clear();
                }
            }
        }

        // 마지막 남은 데이터 처리
        if (!batch.isEmpty()) {
            jdbcTemplate.batchUpdate(
                "INSERT INTO user_quiz_answers (created_at, user_answer, is_correct, quiz_id, subscription_id) "
                    +
                    "VALUES (?, ?, ?, ?, ?)",
                batch
            );
        }
    }

    private Set<Long> getRandomSample(List<Long> list, int count) {
        Collections.shuffle(list);
        return new HashSet<>(list.subList(0, Math.min(count, list.size())));
    }


    @Test
    @Order(2)
    @DisplayName("테스트 퀴즈 100만개 넣기")
    void insertQuizzes() {

        if (categories.isEmpty()) {
            List<Long> categoryIds = jdbcTemplate.queryForList(
                "SELECT id FROM quiz_category WHERE parent_id = 9", Long.class);

            for (Long id : categoryIds) {
                QuizCategory category = QuizCategory.builder().build();
                ReflectionTestUtils.setField(category, "id", id);
                categories.add(category);
            }
        }

        Random random = new Random();
        Faker faker = new Faker();
        int batchSize = 5000;
        List<Object[]> batch = new ArrayList<>();
        List<QuizFormatType> types = List.of(QuizFormatType.MULTIPLE_CHOICE,
            QuizFormatType.SHORT_ANSWER, QuizFormatType.SUBJECTIVE);

        List<QuizLevel> levels = List.of(QuizLevel.EASY,
            QuizLevel.NORMAL, QuizLevel.HARD);

        for (int i = 0; i < 850_000; i++) {
            QuizCategory category = categories.get(i % categories.size());

            batch.add(new Object[]{
                Timestamp.valueOf(LocalDateTime.now().minusDays(random.nextInt(90))),
                types.get(i % 2).name(),
                "Q" + faker.yoda().quote() + i,
                "A" + faker.lorem().paragraph() + i,
                "Commentary " + faker.chuckNorris().fact() + i,
                "1. A / 2. B / 3. C / 4. D" + faker.lorem().sentence(),
                category.getId(),
                false,
                levels.get(i % 2).name(),
            });

            if (batch.size() >= batchSize) {
                jdbcTemplate.batchUpdate(
                    "INSERT INTO quiz (created_at,type, question, answer, commentary, choice, "
                        + "quiz_category_id, is_deleted, level) "
                        +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    batch
                );
                batch.clear();
            }
        }

        // 마지막 남은 데이터 처리
        if (!batch.isEmpty()) {
            jdbcTemplate.batchUpdate(
                "INSERT INTO quiz (created_at,type, question, answer, commentary, choice, "
                    + "quiz_category_id, is_deleted, level) "
                    +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                batch
            );
        }
    }

    @Test
    @Order(3)
    @DisplayName("구독 만개 넣기")
    void insertSubscriptions() {
        Random random = new Random();
        List<Object[]> batch = new ArrayList<>();

        List<Integer> subscriptionTypes = List.of(
            2, 32, 10, 20, 42, 84, 62, 65, 43, 127
        );

        for (int i = 0; i < 10_000; i++) {
            batch.add(new Object[]{
                Timestamp.valueOf(LocalDateTime.now().minusDays(random.nextInt(90))),
                parent.getId(),
                "user" + i + "@test.com",
                true,
                subscriptionTypes.get(i % subscriptionTypes.size())
            });
        }

        jdbcTemplate.batchUpdate(
            "INSERT INTO subscription (created_at, quiz_category_id, email, is_active, subscription_type) "
                + "VALUES (?, ?, ?, ?,?)",
            batch
        );
    }

}