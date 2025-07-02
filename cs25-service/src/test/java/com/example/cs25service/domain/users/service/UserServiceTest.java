package com.example.cs25service.domain.users.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.subscription.entity.DayOfWeek;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25entity.domain.user.entity.Role;
import com.example.cs25entity.domain.user.entity.SocialType;
import com.example.cs25entity.domain.user.entity.User;
import com.example.cs25entity.domain.user.exception.UserException;
import com.example.cs25entity.domain.user.exception.UserExceptionCode;
import com.example.cs25entity.domain.user.repository.UserRepository;
import com.example.cs25service.domain.security.dto.AuthUser;
import com.example.cs25service.domain.subscription.service.SubscriptionService;
import java.time.LocalDate;
import java.util.EnumSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("disableUser 메서드는")
    class disableUser {

        private final AuthUser mockAuthUser = new AuthUser("nickname", "sub-uuid-1", Role.USER);

        @Test
        @DisplayName("유저가 존재하고 구독 정보가 존재하면 모두 비활성화 된다.")
        void existUser_existSubscription_success() {
            //given
            Subscription subscription = Subscription.builder()
                .category(QuizCategory.builder()
                    .categoryType("BACKEND")
                    .build())
                .email("test@naver.com")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .subscriptionType(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY))
                .build();
            ReflectionTestUtils.setField(subscription, "id", 1L);
            ReflectionTestUtils.setField(subscription, "serialId", "sub-uuid-1");

            User user = User.builder().name("test").email("test@example.com")
                .role(Role.USER).subscription(subscription)
                .socialType(SocialType.KAKAO).build();
            ReflectionTestUtils.setField(user, "id", 1L);
            ReflectionTestUtils.setField(user, "serialId", "sub-uuid-1");

            when(userRepository.findBySerialIdOrElseThrow(subscription.getSerialId()))
                .thenReturn(user);

            //when
            userService.disableUser(mockAuthUser);

            // then
            assertThat(user.isActive()).isFalse();
            verify(subscriptionService).cancelSubscription("sub-uuid-1");
        }

        @Test
        @DisplayName("유저가 존재하지만 구독이 없으면 유저만 비활성화 된다.")
        void existUser_noSubscription_success() {
            // given
            User user = User.builder()
                .name("test")
                .email("test@example.com")
                .role(Role.USER)
                .subscription(null)
                .socialType(SocialType.KAKAO)
                .build();
            ReflectionTestUtils.setField(user, "id", 2L);
            ReflectionTestUtils.setField(user, "serialId", "sub-uuid-1");

            when(userRepository.findBySerialIdOrElseThrow("sub-uuid-1"))
                .thenReturn(user);

            // when
            userService.disableUser(mockAuthUser);

            // then
            assertThat(user.isActive()).isFalse();
            verify(subscriptionService, never()).cancelSubscription(any());
        }

        @Test
        @DisplayName("유저가 존재하지 않으면 예외를 던진다.")
        void noUser_throwException() {
            // given
            when(userRepository.findBySerialIdOrElseThrow("sub-uuid-1"))
                .thenThrow(new UserException(UserExceptionCode.NOT_FOUND_USER));

            // when & then
            assertThatThrownBy(() -> userService.disableUser(mockAuthUser))
                .isInstanceOf(UserException.class)
                .hasMessageContaining(UserExceptionCode.NOT_FOUND_USER.getMessage());
        }
    }
}