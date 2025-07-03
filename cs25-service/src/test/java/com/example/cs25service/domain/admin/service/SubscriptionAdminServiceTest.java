package com.example.cs25service.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.repository.QuizCategoryRepository;
import com.example.cs25entity.domain.subscription.entity.DayOfWeek;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25entity.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25service.domain.admin.dto.response.SubscriptionPageResponseDto;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SubscriptionAdminServiceTest {

    @InjectMocks
    private SubscriptionAdminService subscriptionAdminService;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private QuizCategoryRepository categoryRepository; // assuming a Category repository is used

    QuizCategory parentCategory;
    QuizCategory subCategory1;
    Subscription subscription;

    @BeforeEach
    void setUp() {
        // 상위 카테고리와 하위 카테고리 mock
        parentCategory = QuizCategory.builder()
            .categoryType("Backend")
            .build();

        subCategory1 = QuizCategory.builder()
            .categoryType("InformationSystemManagement")
            .parent(parentCategory)
            .build();

        subscription = Subscription.builder()
            .email("test@example.com")
            .category(parentCategory)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusMonths(1))
            .subscriptionType(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY))
            .build();

        ReflectionTestUtils.setField(parentCategory, "children", List.of(subCategory1));
        ReflectionTestUtils.setField(subscription, "id", 1L);
        ReflectionTestUtils.setField(subscription, "serialId", "serial-subscription-001");
    }

    @Nested
    @DisplayName("getAdminSubscriptions 함수는")
    class inGetAdminSubscriptions {
        
        @Test
        @DisplayName("정상작동_시_구독리스트가_반환된다")
        void getAdminSubscriptions_success() {
            // given
            int page = 1;
            int size = 10;

            // Page<Subscription> 객체 생성
            Page<Subscription> subscriptionPage = new PageImpl<>(List.of(subscription));

            given(subscriptionRepository.findAllByOrderByIdAsc(any(Pageable.class)))
                .willReturn(subscriptionPage);

            // when
            Page<SubscriptionPageResponseDto> result = subscriptionAdminService.getAdminSubscriptions(
                page, size);

            // then
            assertThat(result.getContent()).isNotNull();  // null 검사
            assertThat(result.getContent()).hasSize(1);   // 크기 확인
            SubscriptionPageResponseDto dto = result.getContent().get(0);
            assertThat(dto.getId()).isEqualTo(1L);
            assertThat(dto.getCategory()).isEqualTo("Backend");
            assertThat(dto.getEmail()).isEqualTo("test@example.com");
            assertThat(dto.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("getSubscription 함수는")
    class inGetSubscription {

        @Test
        @DisplayName("정상작동_시_구독자가_반환된다")
        void getSubscription_success() {
            // given
            Long subscriptionId = 1L;

            given(subscriptionRepository.findByIdOrElseThrow(subscriptionId))
                .willReturn(subscription);

            // when
            SubscriptionPageResponseDto result = subscriptionAdminService.getSubscription(
                subscriptionId);

            // then
            assertThat(result.getId()).isEqualTo(subscriptionId);
            assertThat(result.getCategory()).isEqualTo("Backend");
            assertThat(result.getEmail()).isEqualTo("test@example.com");
            assertThat(result.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("deleteSubscription 함수는")
    class inDeleteSubscription {

        @Test
        @DisplayName("정상작동_시_구독자가_비활성화된다")
        void deleteSubscription_success() {
            // given
            Long subscriptionId = 1L;

            given(subscriptionRepository.findByIdOrElseThrow(subscriptionId))
                .willReturn(subscription);

            // when
            subscriptionAdminService.deleteSubscription(subscriptionId);

            // then
            assertThat(
                subscription.isActive()).isFalse();
        }
    }
}
