package com.example.cs25entity.domain.subscription.entity;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.example.cs25common.global.config.JpaAuditingConfig;
import com.example.cs25entity.config.QuerydslConfig;
import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.repository.QuizCategoryRepository;
import com.example.cs25entity.domain.subscription.exception.SubscriptionHistoryException;
import com.example.cs25entity.domain.subscription.repository.SubscriptionHistoryRepository;
import com.example.cs25entity.domain.subscription.repository.SubscriptionRepository;

@DataJpaTest
@Import({QuerydslConfig.class, JpaAuditingConfig.class}) // QueryDsl, Jpa 설정
@ActiveProfiles("test") // application-test.properties 사용 선언
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // DB 설정이 그대로 사용됨 (application-test.properties 기반)
class SubscriptionHistoryTest {
	@Autowired SubscriptionHistoryRepository subscriptionHistoryRepository;
	@Autowired SubscriptionRepository subscriptionRepository;
	@Autowired QuizCategoryRepository quizCategoryRepository;

	@DisplayName("구독 히스토리 빌드생성자 동작 테스트")
	@Test
	void builder() {
		// given
		Subscription subscription = createSubscription();
		LocalDate startDate = LocalDate.of(2025, 1, 1);
		LocalDate updateDate = LocalDate.of(2025, 1, 15);
		Set<DayOfWeek> historyDays = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
		int encodedDays = Subscription.encodeDays(historyDays);

		// when
		SubscriptionHistory history = SubscriptionHistory.builder()
			.subscription(subscription)
			.category(subscription.getCategory())
			.startDate(startDate)
			.updateDate(updateDate)
			.subscriptionType(encodedDays)
			.build();
		subscriptionHistoryRepository.save(history);

		// then
		assertEquals(subscription, history.getSubscription());
		assertEquals(subscription.getCategory(), history.getCategory());
		assertEquals(startDate, history.getStartDate());
		assertEquals(updateDate, history.getUpdateDate());
		assertEquals(encodedDays, history.getSubscriptionType());
		assertNotNull(history.getId());
	}

	@DisplayName("특정 구독의 모든 히스토리 조회")
	@Test
	void findAllBySubscriptionId() {
		// given
		Subscription subscription = createSubscription();
		SubscriptionHistory history1 = createSubscriptionHistory(subscription, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 15));
		SubscriptionHistory history2 = createSubscriptionHistory(subscription, LocalDate.of(2025, 1, 16), LocalDate.of(2025, 2, 1));

		// when
		List<SubscriptionHistory> histories = subscriptionHistoryRepository.findAllBySubscriptionId(subscription.getId());

		// then
		assertEquals(2, histories.size());
		assertTrue(histories.contains(history1));
		assertTrue(histories.contains(history2));
	}

	@DisplayName("구독 히스토리 ID로 조회 - 성공")
	@Test
	void findByIdOrElseThrow_success() {
		// given
		Subscription subscription = createSubscription();
		SubscriptionHistory history = createSubscriptionHistory(subscription, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 15));

		// when
		SubscriptionHistory foundHistory = subscriptionHistoryRepository.findByIdOrElseThrow(history.getId());

		// then
		assertEquals(history.getId(), foundHistory.getId());
		assertEquals(history.getSubscription().getId(), foundHistory.getSubscription().getId());
	}

	@DisplayName("구독 히스토리 ID로 조회 - 실패")
	@Test
	void findByIdOrElseThrow_fail() {
		// given
		Long subscriptionId = 999L;

		// when & then
		SubscriptionHistoryException ex = assertThrows(SubscriptionHistoryException.class, () ->
			subscriptionHistoryRepository.findByIdOrElseThrow(subscriptionId)
		);
		assertThat(ex.getMessage()).contains("존재하지 않는 구독 내역입니다.");
	}

	@DisplayName("구독 히스토리 기간 검증")
	@Test
	void historyPeriodValidation() {
		// given
		Subscription subscription = createSubscription();
		LocalDate startDate = LocalDate.of(2025, 1, 1);
		LocalDate updateDate = LocalDate.of(2025, 1, 15);

		// when
		SubscriptionHistory history = createSubscriptionHistory(subscription, startDate, updateDate);

		// then
		assertTrue(history.getStartDate().isBefore(history.getUpdateDate()));
		assertEquals(14, history.getStartDate().until(history.getUpdateDate()).getDays());
	}

	private Subscription createSubscription() {
		QuizCategory category = QuizCategory.builder()
			.categoryType("BACKEND")
			.build();
		quizCategoryRepository.save(category);

		Set<DayOfWeek> days = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
		
		return subscriptionRepository.save(
			Subscription.builder()
				.email("test@example.com")
				.startDate(LocalDate.of(2025, 1, 1))
				.endDate(LocalDate.of(2025, 3, 1))
				.subscriptionType(days)
				.category(category)
				.build()
		);
	}

	private SubscriptionHistory createSubscriptionHistory(Subscription subscription, LocalDate startDate, LocalDate updateDate) {
		Set<DayOfWeek> historyDays = EnumSet.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY);
		
		return subscriptionHistoryRepository.save(
			SubscriptionHistory.builder()
				.subscription(subscription)
				.category(subscription.getCategory())
				.startDate(startDate)
				.updateDate(updateDate)
				.subscriptionType(Subscription.encodeDays(historyDays))
				.build()
		);
	}
}