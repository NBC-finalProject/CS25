package com.example.cs25.domain.users.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;

import com.example.cs25.domain.oauth2.dto.SocialType;
import com.example.cs25.domain.quiz.entity.QuizCategory;
import com.example.cs25.domain.subscription.dto.SubscriptionHistoryDto;
import com.example.cs25.domain.subscription.dto.SubscriptionInfoDto;
import com.example.cs25.domain.subscription.entity.DayOfWeek;
import com.example.cs25.domain.subscription.entity.Subscription;
import com.example.cs25.domain.subscription.entity.SubscriptionHistory;
import com.example.cs25.domain.subscription.repository.SubscriptionHistoryRepository;
import com.example.cs25.domain.subscription.service.SubscriptionService;
import com.example.cs25.domain.users.dto.UserProfileResponse;
import com.example.cs25.domain.users.entity.Role;
import com.example.cs25.domain.users.entity.User;
import com.example.cs25.domain.users.exception.UserException;
import com.example.cs25.domain.users.exception.UserExceptionCode;
import com.example.cs25.domain.users.repository.UserRepository;
import com.example.cs25.global.dto.AuthUser;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private SubscriptionHistoryRepository subscriptionHistoryRepository;

    private Long subscriptionId = 1L;
    private Subscription subscription;
    private Long userId = 1L;
    private User user;

    @BeforeEach
    void setUp() {
        subscription = Subscription.builder()
            .subscriptionType(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY))
            .startDate(LocalDate.of(2024, 1, 1))
            .endDate(LocalDate.of(2024, 1, 31))
            .category(new QuizCategory(1L, "BACKEND"))
            .build();

        ReflectionTestUtils.setField(subscription, "id", subscriptionId);

        user = User.builder()
            .email("test@email.com")
            .name("홍길동")
            .socialType(SocialType.KAKAO)
            .role(Role.USER)
            .subscription(subscription)
            .build();
        ReflectionTestUtils.setField(user, "id", userId);

    }


    @Test
    void getUserProfile_정상조회() {
        //given
        QuizCategory quizCategory = new QuizCategory(1L, "BACKEND");
        AuthUser authUser = new AuthUser(userId, "test@email.com", "testUser", Role.USER);

        SubscriptionHistory log1 = SubscriptionHistory.builder()
            .category(quizCategory)
            .subscription(subscription)
            .subscriptionType(64)
            .build();
        SubscriptionHistory log2 = SubscriptionHistory.builder()
            .category(quizCategory)
            .subscription(subscription)
            .subscriptionType(26)
            .build();

        SubscriptionInfoDto subscriptionInfoDto = new SubscriptionInfoDto(
            quizCategory,
            30L,
            Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY)
        );

        SubscriptionHistoryDto dto1 = SubscriptionHistoryDto.fromEntity(log1);
        SubscriptionHistoryDto dto2 = SubscriptionHistoryDto.fromEntity(log2);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(subscriptionService.getSubscription(subscriptionId)).willReturn(subscriptionInfoDto);
        given(subscriptionHistoryRepository.findAllBySubscriptionId(subscriptionId))
            .willReturn(List.of(log1, log2));

        try (MockedStatic<SubscriptionHistoryDto> mockedStatic = mockStatic(
            SubscriptionHistoryDto.class)) {
            mockedStatic.when(() -> SubscriptionHistoryDto.fromEntity(log1)).thenReturn(dto1);
            mockedStatic.when(() -> SubscriptionHistoryDto.fromEntity(log2)).thenReturn(dto2);

            // whene
            UserProfileResponse response = userService.getUserProfile(authUser);

            // then
            assertThat(response.getUserId()).isEqualTo(userId);
            assertThat(response.getEmail()).isEqualTo(user.getEmail());
            assertThat(response.getName()).isEqualTo(user.getName());
            assertThat(response.getSubscriptionInfoDto()).isEqualTo(subscriptionInfoDto);
            assertThat(response.getSubscriptionLogPage()).containsExactly(dto1, dto2);
        }
    }


    @Test
    void getUserProfile_유저없음_예외() {
        // given
        Long invalidUserId = 999L;
        AuthUser authUser = new AuthUser(invalidUserId, "no@email.com", "ghost", Role.USER);
        given(userRepository.findById(invalidUserId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUserProfile(authUser))
            .isInstanceOf(UserException.class)
            .hasMessageContaining(UserExceptionCode.NOT_FOUND_USER.getMessage());
    }

    @Test
    void disableUser_정상작동() {
        // given
        AuthUser authUser = new AuthUser(userId, user.getEmail(), user.getName(), user.getRole());
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        userService.disableUser(authUser);

        // then
        assertThat(user.isActive()).isFalse(); // isActive()가 updateDisableUser()에 의해 true가 됐다고 가정
    }

    @Test
    void disableUser_유저없음_예외() {
        // given
        Long invalidUserId = 999L;
        AuthUser authUser = new AuthUser(invalidUserId, "no@email.com", "ghost", Role.USER);
        given(userRepository.findById(invalidUserId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.disableUser(authUser))
            .isInstanceOf(UserException.class)
            .hasMessageContaining(UserExceptionCode.NOT_FOUND_USER.getMessage());
    }

}