package com.example.cs25service.domain.mail.service;

import com.example.cs25entity.domain.mail.dto.MailLogSearchDto;
import com.example.cs25entity.domain.mail.entity.MailLog;
import com.example.cs25entity.domain.mail.repository.MailLogCustomRepository;
import com.example.cs25entity.domain.mail.repository.MailLogRepository;
import com.example.cs25service.domain.mail.dto.MailLogResponse;
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
    private final MailLogCustomRepository mailLogCustomRepository;

    //전체 로그 페이징 조회
    @Transactional(readOnly = true)
    public Page<MailLogResponse> getMailLogs(MailLogSearchDto condition, Pageable pageable) {

        //시작일과 종료일 모두 설정했을 때
        if (condition.getStartDate() != null && condition.getEndDate() != null) {
            if (condition.getStartDate().isAfter(condition.getEndDate())) {
                throw new IllegalArgumentException("시작일은 종료일보다 이후일 수 없습니다.");
            }
        }

        return mailLogCustomRepository.search(condition, pageable)
            .map(MailLogResponse::from);
    }

    //단일 로그 조회
    @Transactional(readOnly = true)
    public MailLogResponse getMailLog(Long id) {
        MailLog mailLog = mailLogRepository.findByIdOrElseThrow(id);
        return MailLogResponse.from(mailLog);
    }

    @Transactional
    public void deleteMailLogs(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("삭제할 로그 데이터가 없습니다.");
        }

        mailLogRepository.deleteAllByIdIn(ids);
    }
}
