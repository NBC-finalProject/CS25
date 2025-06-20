package com.example.cs25service.domain.admin.service;

import static com.example.cs25entity.domain.subscription.entity.Subscription.decodeDays;

import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25entity.domain.subscription.entity.SubscriptionHistory;
import com.example.cs25entity.domain.subscription.repository.SubscriptionHistoryRepository;
import com.example.cs25entity.domain.user.entity.User;
import com.example.cs25entity.domain.user.exception.UserException;
import com.example.cs25entity.domain.user.exception.UserExceptionCode;
import com.example.cs25entity.domain.user.repository.UserRepository;
import com.example.cs25service.domain.admin.dto.response.UserDetailResponseDto;
import com.example.cs25service.domain.admin.dto.response.UserPageResponseDto;
import com.example.cs25service.domain.subscription.dto.SubscriptionHistoryDto;
import com.example.cs25service.domain.subscription.dto.SubscriptionInfoDto;
import com.example.cs25service.domain.subscription.dto.SubscriptionRequestDto;
import com.example.cs25service.domain.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;

    @Transactional(readOnly = true)
    public Page<UserPageResponseDto> getAdminUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<User> userPage = userRepository.findAllByOrderByIdAsc(pageable);

        return userPage.map(user ->
            UserPageResponseDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .isActive(user.isActive())
                .name(user.getName())
                .role(user.getRole().name())
                .socialType(user.getSocialType().name())
                .build());
    }

    @Transactional(readOnly = true)
    public UserDetailResponseDto getAdminUserDetail(Long userId) {
        User user = userRepository.findByIdOrElseThrow(userId);

        UserPageResponseDto userInfo = UserPageResponseDto.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .isActive(user.isActive())
            .name(user.getName())
            .role(user.getRole().name())
            .socialType(user.getSocialType().name())
            .build();

        Subscription subscription = user.getSubscription();

        if (subscription == null) {
            return UserDetailResponseDto.builder()
                .subscriptionInfo(null)
                .subscriptionLog(null)
                .userInfo(userInfo)
                .build();
        } else {

            //구독 시작, 구독 종료 날짜 기반으로 구독 기간 계산
            LocalDate start = subscription.getStartDate();
            LocalDate end = subscription.getEndDate();
            long period = ChronoUnit.DAYS.between(start, end);

            SubscriptionInfoDto subscriptionInfo = SubscriptionInfoDto.builder()
                .category(subscription.getCategory().getCategoryType())
                .email(subscription.getEmail())
                .days(decodeDays(subscription.getSubscriptionType()))
                .active(subscription.isActive())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .period(period)
                .build();

            //로그 다 모아와서 리스트로 만들기
            List<SubscriptionHistory> subLogs = subscriptionHistoryRepository
                .findAllBySubscriptionId(subscription.getId());
            List<SubscriptionHistoryDto> dtoList = subLogs.stream()
                .map(SubscriptionHistoryDto::fromEntity)
                .toList();

            return UserDetailResponseDto.builder()
                .subscriptionInfo(subscriptionInfo)
                .subscriptionLog(dtoList)
                .userInfo(userInfo)
                .build();
        }
    }

    @Transactional
    public void disableUser(@Positive Long userId) {
        User user = userRepository.findByIdOrElseThrow(userId);

        if (!user.isActive()) {
            throw new UserException(UserExceptionCode.INACTIVE_USER);
        }

        if (user.getSubscription() != null) {
            user.getSubscription().updateDisable(); //구독도 취소
        }

        user.updateDisableUser();
    }

    @Transactional
    public void updateSubscription(@Positive Long userId,
        @Valid SubscriptionRequestDto request) {
        User user = userRepository.findByIdOrElseThrow(userId);

        if (user.getSubscription() == null) {
            throw new UserException(UserExceptionCode.NOT_FOUND_SUBSCRIPTION);
        }

        subscriptionService.updateSubscription(user.getSubscription().getId(), request);
    }

    @Transactional
    public void cancelSubscription(@Positive Long userId) {
        User user = userRepository.findByIdOrElseThrow(userId);

        if (user.getSubscription() == null) {
            throw new UserException(UserExceptionCode.NOT_FOUND_SUBSCRIPTION);
        }

        subscriptionService.cancelSubscription(user.getSubscription().getId());
    }
}
