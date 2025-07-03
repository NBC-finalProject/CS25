package com.example.cs25service.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.subscription.entity.DayOfWeek;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25entity.domain.subscription.entity.SubscriptionHistory;
import com.example.cs25entity.domain.subscription.repository.SubscriptionHistoryRepository;
import com.example.cs25entity.domain.user.entity.Role;
import com.example.cs25entity.domain.user.entity.SocialType;
import com.example.cs25entity.domain.user.entity.User;
import com.example.cs25entity.domain.user.exception.UserException;
import com.example.cs25entity.domain.user.repository.UserRepository;
import com.example.cs25service.domain.admin.dto.request.UserRoleUpdateRequestDto;
import com.example.cs25service.domain.admin.dto.response.UserDetailResponseDto;
import com.example.cs25service.domain.admin.dto.response.UserPageResponseDto;
import com.example.cs25service.domain.subscription.dto.SubscriptionRequestDto;
import com.example.cs25service.domain.subscription.service.SubscriptionService;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    @InjectMocks
    private UserAdminService userAdminService;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionHistoryRepository subscriptionHistoryRepository;

    QuizCategory parentCategory;
    User user;
    Subscription subscription;

    @BeforeEach
    void setUp() {
        // 상위 카테고리와 하위 카테고리 mock
        parentCategory = QuizCategory.builder()
            .categoryType("Backend")
            .build();

        subscription = Subscription.builder()
            .startDate(LocalDate.now().minusDays(10))
            .endDate(LocalDate.now())
            .subscriptionType(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY))
            .email("sub@email.com")
            .category(parentCategory)
            .build();

        user = User.builder()
            .email("test@email.com")
            .name("테스트")
            .role(Role.USER)
            .socialType(SocialType.KAKAO)
            .score(0)
            .build();

        ReflectionTestUtils.setField(user, "id", 1L);
    }

    @Nested
    @DisplayName("getAdminUsers() 는 ")
    class GetAdminUsersTest {

        @Test
        @DisplayName("페이지네이션된 유저 리스트 반환")
        void getAdminUsers_success() {
            // given
            user.updateSubscription(subscription);

            Pageable pageable = PageRequest.of(0, 10);
            Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);

            given(userRepository.findAllByOrderByIdAsc(pageable)).willReturn(userPage);

            // when
            Page<UserPageResponseDto> result = userAdminService.getAdminUsers(1, 10);

            // then
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getEmail()).isEqualTo("test@email.com");
        }
    }

    @Nested
    @DisplayName("getAdminUserDetail()")
    class GetAdminUserDetailTest {

        @Test
        @DisplayName("구독이 없는 유저 상세 정보 반환")
        void getUserDetail_noSubscription() {
            // given
            given(userRepository.findByIdOrElseThrow(1L)).willReturn(user);

            // when
            UserDetailResponseDto result = userAdminService.getAdminUserDetail(1L);

            // then
            assertThat(result.getUserInfo().getEmail()).isEqualTo("test@email.com");
            assertThat(result.getSubscriptionInfo()).isNull();
            assertThat(result.getSubscriptionLog()).isNull();
        }

        @Test
        @DisplayName("구독이 있는 유저 상세 정보 반환")
        void getUserDetail_withSubscription() {
            // given
            ReflectionTestUtils.setField(subscription, "id", 1L);
            user.updateSubscription(subscription);

            SubscriptionHistory history = SubscriptionHistory.builder()
                .subscription(subscription)
                .category(parentCategory)
                .build();

            given(userRepository.findByIdOrElseThrow(1L)).willReturn(user);
            given(subscriptionHistoryRepository.findAllBySubscriptionId(1L)).willReturn(
                List.of(history));

            // when
            UserDetailResponseDto result = userAdminService.getAdminUserDetail(1L);

            // then
            assertThat(result.getUserInfo().getEmail()).isEqualTo("test@email.com");
            assertThat(result.getSubscriptionInfo()).isNotNull();
            assertThat(result.getSubscriptionLog()).hasSize(1);
        }
    }


    @Nested
    @DisplayName("disableUser()")
    class DisableUserTest {

        @Test
        @DisplayName("활성 유저를 비활성화 처리")
        void disable_active_user() {
            User user = mock(User.class);
            given(user.isActive()).willReturn(true);
            given(user.getSubscription()).willReturn(null);
            given(userRepository.findByIdOrElseThrow(1L)).willReturn(user);

            userAdminService.disableUser(1L);

            verify(user).updateDisableUser();
        }

        @Test
        @DisplayName("이미 비활성화된 유저 예외 발생")
        void disable_inactive_user_throws() {
            User user = mock(User.class);
            given(user.isActive()).willReturn(false);
            given(userRepository.findByIdOrElseThrow(1L)).willReturn(user);

            assertThatThrownBy(() -> userAdminService.disableUser(1L))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("이미 삭제된 유저입니다.");
        }
    }

    @Nested
    @DisplayName("updateSubscription()")
    class UpdateSubscriptionTest {

        @Test
        @DisplayName("구독 정보 수정 성공")
        void update_subscription_success() {
            Subscription subscription = mock(Subscription.class);
            User user = mock(User.class);
            given(user.getSubscription()).willReturn(subscription);
            given(subscription.getSerialId()).willReturn("sub-123");
            given(userRepository.findByIdOrElseThrow(1L)).willReturn(user);

            SubscriptionRequestDto request = mock(SubscriptionRequestDto.class);

            userAdminService.updateSubscription(1L, request);

            verify(subscriptionService).updateSubscription("sub-123", request);
        }

        @Test
        @DisplayName("구독 정보 없음 예외 발생")
        void update_subscription_not_found() {
            User user = mock(User.class);
            given(user.getSubscription()).willReturn(null);
            given(userRepository.findByIdOrElseThrow(1L)).willReturn(user);

            assertThatThrownBy(
                () -> userAdminService.updateSubscription(1L, mock(SubscriptionRequestDto.class)))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("해당 유저에게 구독 정보가 없습니다.");
        }
    }

    @Nested
    @DisplayName("cancelSubscription()")
    class CancelSubscriptionTest {

        @Test
        @DisplayName("구독 취소 성공")
        void cancel_subscription_success() {

            Subscription subscription = mock(Subscription.class);
            User user = mock(User.class);
            given(user.getSubscription()).willReturn(subscription);
            given(subscription.getSerialId()).willReturn("sub-456");
            given(userRepository.findByIdOrElseThrow(1L)).willReturn(user);

            userAdminService.cancelSubscription(1L);

            verify(subscriptionService).cancelSubscription("sub-456");
        }

        @Test
        @DisplayName("구독 없음 예외 발생")
        void cancel_subscription_not_found() {
            User user = mock(User.class);
            given(user.getSubscription()).willReturn(null);
            given(userRepository.findByIdOrElseThrow(1L)).willReturn(user);

            assertThatThrownBy(() -> userAdminService.cancelSubscription(1L))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("해당 유저에게 구독 정보가 없습니다.");
        }
    }

    @Nested
    @DisplayName("patchUserRole()")
    class PatchUserRoleTest {

        @Test
        @DisplayName("역할이 다르면 업데이트 수행")
        void patch_user_role_success() {
            User user = mock(User.class);
            given(user.getRole()).willReturn(Role.USER);
            given(userRepository.findByIdOrElseThrow(1L)).willReturn(user);

            UserRoleUpdateRequestDto request = mock(UserRoleUpdateRequestDto.class);
            given(request.getRole()).willReturn(Role.ADMIN);

            userAdminService.patchUserRole(1L, request);

            verify(user).updateRole(Role.ADMIN);
        }

        @Test
        @DisplayName("요청 역할이 null이면 예외")
        void patch_user_role_null() {
            User user = mock(User.class);
            given(userRepository.findByIdOrElseThrow(1L)).willReturn(user);

            UserRoleUpdateRequestDto request = mock(UserRoleUpdateRequestDto.class);
            given(request.getRole()).willReturn(null);

            assertThatThrownBy(() -> userAdminService.patchUserRole(1L, request))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("역할 값이 잘못되었습니다.");
        }
    }

}
