package com.example.cs25service.domain.mail.service;

import com.example.cs25entity.domain.mail.dto.MailLogSearchDto;
import com.example.cs25entity.domain.mail.entity.MailLog;
import com.example.cs25entity.domain.mail.repository.MailLogCustomRepositoryImpl;
import com.example.cs25entity.domain.mail.repository.MailLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailLogService {
    private final MailLogRepository mailLogRepository;
    private final MailLogCustomRepositoryImpl mailLogCustomRepository;

    //전체 로그 페이징 조회
    public Page<MailLog> getMailLogs(MailLogSearchDto condition, Pageable pageable) {
        return mailLogCustomRepository.search(condition, pageable);
    }

    //단일 로그 조회
}
