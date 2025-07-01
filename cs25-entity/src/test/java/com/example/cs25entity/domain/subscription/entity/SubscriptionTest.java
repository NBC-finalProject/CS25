package com.example.cs25entity.domain.subscription.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.EnumSet;
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
import com.example.cs25entity.domain.subscription.repository.SubscriptionRepository;

@DataJpaTest
@Import({QuerydslConfig.class, JpaAuditingConfig.class}) // QueryDsl, Jpa 설정
@ActiveProfiles("test") // application-test.properties 사용 선언
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // DB 설정이 그대로 사용됨 (application-test.properties 기반)
class SubscriptionTest {
	@Autowired SubscriptionRepository subscriptionRepository;
	@Autowired QuizCategoryRepository quizCategoryRepository;

	@DisplayName("구독 빌드생성자 동작 테스트")
	@Test
	void builder() {
	    // given
		Set<DayOfWeek> selectedDays = EnumSet.of(
			DayOfWeek.MONDAY,
			DayOfWeek.WEDNESDAY,
			DayOfWeek.FRIDAY
		);

		// when
		Subscription subscription = createSubscription(selectedDays);

		// then
		assertEquals("123@123.com", subscription.getEmail());
		assertTrue(subscription.isActive());
		assertEquals(Subscription.encodeDays(selectedDays), subscription.getSubscriptionType());
		assertNotNull(subscription.getSerialId());
	}

	@DisplayName("구독요일 인코딩/디코딩 테스트")
	@Test
	void encodeAndDecodeDays() {
	    // given
		Set<DayOfWeek> days = EnumSet.of(
			DayOfWeek.MONDAY,
			DayOfWeek.WEDNESDAY,
			DayOfWeek.FRIDAY
		);

	    // when
		int encodedDays = Subscription.encodeDays(days);
		Set<DayOfWeek> decodedDays = Subscription.decodeDays(encodedDays);

	    // then
		assertEquals(days, decodedDays);
		assertEquals(3, decodedDays.size());
		assertTrue(decodedDays.contains(DayOfWeek.MONDAY));
		assertTrue(decodedDays.contains(DayOfWeek.WEDNESDAY));
		assertTrue(decodedDays.contains(DayOfWeek.FRIDAY));
		assertFalse(decodedDays.contains(DayOfWeek.SUNDAY));
		assertFalse(decodedDays.contains(DayOfWeek.SATURDAY));
		assertFalse(decodedDays.contains(DayOfWeek.TUESDAY));
		assertFalse(decodedDays.contains(DayOfWeek.THURSDAY));
	}

	@DisplayName("오늘이 구독한날이면 true를 반환")
	@Test
	void isTodaySubscribed_true () {
		// given
		Set<DayOfWeek> allSubDays = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
			DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

		// when
		Subscription subscription = createSubscription(allSubDays);

		// then
		assertTrue(subscription.isTodaySubscribed());
	}

	@DisplayName("오늘이 구독한날이 아니면 false를 반환")
	@Test
	void isTodaySubscribed_false () {
	    // given
		int todayIndex = LocalDate.now().getDayOfWeek().getValue() % 7;
		int yesterdayIndex = (todayIndex - 1 + 7) % 7;
		DayOfWeek yesterday = DayOfWeek.values()[yesterdayIndex];

		Set<DayOfWeek> subYesterdays = EnumSet.of(yesterday); // 항상 전날만 구독하고 있음

		// when
		Subscription subscription = createSubscription(subYesterdays);

		// then
		assertFalse(subscription.isTodaySubscribed());
	}

	@DisplayName("구독 정보를 업데이트")
	@Test
	void subscriptionUpdate() {
	    // given
		Subscription subscription = createSubscription(
			EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));

		QuizCategory updateCategory = QuizCategory.builder()
			.categoryType("FRONTEND")
			.build();
		quizCategoryRepository.save(updateCategory);

		Set<DayOfWeek> subOnlyMondays = EnumSet.of(DayOfWeek.MONDAY);
		SubscriptionPeriod plusOneMonth = SubscriptionPeriod.ONE_MONTH;
		LocalDate endDate = subscription.getEndDate();

		// when
		subscription.update(
			updateCategory,
			subOnlyMondays,
			true,
			plusOneMonth
		);

	    // then
		assertEquals(updateCategory, subscription.getCategory());
		assertEquals(EnumSet.of(DayOfWeek.MONDAY), Subscription.decodeDays(subscription.getSubscriptionType()));
		assertEquals(endDate.plusMonths(1), subscription.getEndDate());
		assertTrue(subscription.isActive());
	}

	@DisplayName("구독취소하는 메서드 실행")
	@Test
	void cancelSubscription () {
	    // given
		Subscription subscription = createSubscription(
			EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));

	    // when
		subscription.updateDisable();

	    // then
		assertFalse(subscription.isActive());
	}

	private Subscription createSubscription(Set<DayOfWeek> subscribeDays) {
		QuizCategory quizCategory = QuizCategory.builder()
			.categoryType("BACKEND")
			.build();
		quizCategoryRepository.save(quizCategory);

		LocalDate startDate = LocalDate.of(2025, 1, 1);
		LocalDate endDate = LocalDate.of(2025, 2, 1);

		return subscriptionRepository.save(
			Subscription.builder()
				.email("123@123.com")
				.startDate(startDate)
				.endDate(endDate)
				.subscriptionType(subscribeDays)
				.category(quizCategory)
				.build()
		);
	}
}