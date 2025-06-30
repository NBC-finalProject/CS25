package com.example.cs25service.domain.mail.service;

import com.example.cs25entity.domain.mail.dto.MailLogSearchDto;
import com.example.cs25entity.domain.mail.entity.MailLog;
import com.example.cs25entity.domain.mail.repository.MailLogRepository;
import com.example.cs25entity.domain.user.entity.Role;
import com.example.cs25entity.domain.user.exception.UserException;
import com.example.cs25entity.domain.user.exception.UserExceptionCode;
import com.example.cs25service.domain.mail.dto.MailLogDetailResponse;
import com.example.cs25service.domain.mail.dto.MailLogResponse;
import com.example.cs25service.domain.security.dto.AuthUser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MailLogService {

    private final MailLogRepository mailLogRepository;

    //전체 로그 페이징 조회
    @Transactional(readOnly = true)
    public Page<MailLogResponse> getMailLogs(AuthUser authUser, MailLogSearchDto condition,
        Pageable pageable) {

        //유저 권한 확인
        if (authUser.getRole() != Role.ADMIN) {
            throw new UserException(UserExceptionCode.UNAUTHORIZED_ROLE);
        }

        //시작일과 종료일 모두 설정했을 때
        if (condition.getStartDate() != null && condition.getEndDate() != null) {
            if (condition.getStartDate().isAfter(condition.getEndDate())) {
                throw new IllegalArgumentException("시작일은 종료일보다 이후일 수 없습니다.");
            }
        }

        return mailLogRepository.search(condition, pageable)
            .map(MailLogResponse::from);
    }

    //단일 로그 조회
    @Transactional(readOnly = true)
    public MailLogDetailResponse getMailLog(AuthUser authUser, Long id) {

        if (authUser.getRole() != Role.ADMIN) {
            throw new UserException(UserExceptionCode.UNAUTHORIZED_ROLE);
        }

        MailLog mailLog = mailLogRepository.findByIdOrElseThrow(id);
        return MailLogDetailResponse.from(mailLog);
    }

    @Transactional
    public void deleteMailLogs(AuthUser authUser, List<Long> ids) {

        if (authUser.getRole() != Role.ADMIN) {
            throw new UserException(UserExceptionCode.UNAUTHORIZED_ROLE);
        }

        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("삭제할 메일 로그를 선택해주세요.");
        }

        mailLogRepository.deleteAllByIdIn(ids);
    }
}
