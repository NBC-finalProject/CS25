package com.example.cs25.domain.mail.repository;

import com.example.cs25.domain.mail.entity.MailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MailLogRepository extends JpaRepository<MailLog, Long> {

}
