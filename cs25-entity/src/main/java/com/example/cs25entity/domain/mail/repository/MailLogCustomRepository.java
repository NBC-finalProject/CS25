package com.example.cs25entity.domain.mail.repository;

import com.example.cs25entity.domain.mail.dto.MailLogSearchDto;
import com.example.cs25entity.domain.mail.entity.MailLog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MailLogCustomRepository {
    Page<MailLog> search(MailLogSearchDto condition, Pageable pageable);
}
