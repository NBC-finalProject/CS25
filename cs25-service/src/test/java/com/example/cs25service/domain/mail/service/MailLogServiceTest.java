package com.example.cs25service.domain.mail.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.cs25entity.domain.mail.dto.MailLogSearchDto;
import com.example.cs25entity.domain.mail.entity.MailLog;
import com.example.cs25entity.domain.mail.enums.MailStatus;
import com.example.cs25entity.domain.mail.repository.MailLogRepository;
import com.example.cs25entity.domain.subscription.entity.DayOfWeek;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25entity.domain.user.entity.Role;
import com.example.cs25entity.domain.user.entity.User;
import com.example.cs25entity.domain.user.exception.UserException;
import com.example.cs25entity.domain.user.exception.UserExceptionCode;
import com.example.cs25service.domain.mail.dto.MailLogDetailResponse;
import com.example.cs25service.domain.mail.dto.MailLogResponse;
import com.example.cs25service.domain.security.dto.AuthUser;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailLogServiceTest {

    @InjectMocks
    private MailLogService mailLogService;

    @Mock
    private MailLogRepository mailLogRepository;

    private AuthUser authUserAdmin;
    private AuthUser authUser;

    @BeforeEach
    public void setUp(){
        User user = User.builder()
            .email("test@test.com")
            .name("test")
            .role(Role.ADMIN)
            .build();
        authUserAdmin = new AuthUser(user);

        User user2 = User.builder()
            .email("test2@test.com")
            .name("test2")
            .role(Role.USER)
            .build();
        authUser = new AuthUser(user2);
    }

    @Test
    @DisplayName("관리자 - 전체 로그 조회 성공")
    void getMailLogs_admin_success() {
        //given
        Subscription subscription = Subscription.builder()
            .email("test@test.com")
            .subscriptionType(Collections.singleton(DayOfWeek.MONDAY))
            .build();

        MailLog mailLog = MailLog.builder()
            .subscription(subscription)
            .status(MailStatus.SENT)
            .build();

        MailLogSearchDto condition = MailLogSearchDto.builder().build();
        Pageable pageable = Pageable.ofSize(10);
        Page<MailLog> mockPage = new PageImpl<>(List.of(mailLog));

        when(mailLogRepository.search(condition, pageable)).thenReturn(mockPage);

        //when
        Page<MailLogResponse> result = mailLogService.getMailLogs(authUserAdmin, condition, pageable);

        //then
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("관리자 - 시작일이 종료일보다 늦으면 IllegalArgumentException 예외를 던짐")
    void getMailLogs_invalidDateRange() {
        //given
        Subscription subscription = Subscription.builder()
            .email("test@test.com")
            .subscriptionType(Collections.singleton(DayOfWeek.MONDAY))
            .build();

        MailLog mailLog = MailLog.builder()
            .subscription(subscription)
            .status(MailStatus.SENT)
            .build();

        MailLogSearchDto condition = MailLogSearchDto.builder()
            .startDate(LocalDate.of(2025,7,1))
            .endDate(LocalDate.of(2024, 7,1))
            .build();

        //when
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            mailLogService.getMailLogs(authUserAdmin, condition, Pageable.ofSize(10)));

        //then
        assertEquals("시작일은 종료일보다 이후일 수 없습니다.", ex.getMessage());
    }

    @Test
    @DisplayName("권한 없는 사용자 - 전체 로그 조회 시 UNAUTHORIZE_ROLE 예외를 던짐")
    void getMailLogs_user_throwUserException() {
        //given
        MailLogSearchDto condition = MailLogSearchDto.builder().build();

        //when
        UserException ex = assertThrows(UserException.class, () ->
            mailLogService.getMailLogs(authUser, condition, Pageable.ofSize(10)));

        //then
        assertEquals(UserExceptionCode.UNAUTHORIZE_ROLE, ex.getErrorCode());
    }

    @Test
    @DisplayName("관리자 - 단일 로그 조회 성공")
    void getMailLog_admin_success() {
        //given
        Subscription subscription = Subscription.builder()
            .email("test@test.com")
            .subscriptionType(Collections.singleton(DayOfWeek.MONDAY))
            .build();

        MailLog mailLog = MailLog.builder()
            .subscription(subscription)
            .status(MailStatus.SENT)
            .build();

        when(mailLogRepository.findByIdOrElseThrow(1L)).thenReturn(mailLog);

        //when
        MailLogDetailResponse result = mailLogService.getMailLog(authUserAdmin, 1L);

        //then
        assertNotNull(result);
    }

    @Test
    @DisplayName("권한 없는 사용자 - 단일 로그 조회 시 UNAUTHORIZE_ROLE 예외를 던짐")
    void getMailLog_user_throwUserException() {
        UserException ex = assertThrows(UserException.class, () ->
            mailLogService.getMailLog(authUser, 1L));

        assertEquals(UserExceptionCode.UNAUTHORIZE_ROLE, ex.getErrorCode());
    }

    @Test
    @DisplayName("관리자 - 로그 삭제 성공")
    void deleteMailLogs_admin_success() {
        List<Long> ids = List.of(1L, 2L);

        mailLogService.deleteMailLogs(authUserAdmin, ids);

        verify(mailLogRepository).deleteAllByIdIn(ids);
    }

    @Test
    @DisplayName("관리자 - 삭제할 ID 리스트가 null이면 IllegalArgumentException 예외를 던짐")
    void deleteMailLogs_listEmpty_throwIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            mailLogService.deleteMailLogs(authUserAdmin, null));

        assertEquals("삭제할 메일 로그를 선택해주세요.", ex.getMessage());
    }

    @Test
    @DisplayName("권한 없는 사용자 - 로그 삭제 시 UNAUTHORIZE_ROLE 예외를 던짐")
    void deleteMailLogs_user_throwUserException() {
        List<Long> ids = List.of(1L);

        UserException ex = assertThrows(UserException.class, () ->
            mailLogService.deleteMailLogs(authUser, ids));

        assertEquals(UserExceptionCode.UNAUTHORIZE_ROLE, ex.getErrorCode());
    }
}