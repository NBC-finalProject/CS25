package com.example.cs25entity.domain.mail.repository;

import com.example.cs25entity.domain.mail.entity.MailLog;
import com.example.cs25entity.domain.mail.exception.CustomMailException;
import com.example.cs25entity.domain.mail.exception.MailExceptionCode;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MailLogRepository extends JpaRepository<MailLog, Long>,
    MailLogCustomRepository {

    Optional<MailLog> findById(Long id);

    default MailLog findByIdOrElseThrow(Long id) {
        return findById(id)
            .orElseThrow(() ->
                new CustomMailException(MailExceptionCode.MAIL_LOG_NOT_FOUND_ERROR));
    }

    void deleteAllByIdIn(Collection<Long> ids);

    @Query("""
        select distinct ml.quiz.id
        from MailLog ml
        where ml.subscription.id = :subscriptionId
    """)
    Set<Long> findDistinctQuiz_IdBySubscription_Id(Long subscriptionId);
}
