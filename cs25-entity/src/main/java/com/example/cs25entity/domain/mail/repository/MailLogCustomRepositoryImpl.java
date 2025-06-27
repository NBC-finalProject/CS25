package com.example.cs25entity.domain.mail.repository;

import com.example.cs25entity.domain.mail.dto.MailLogSearchDto;
import com.example.cs25entity.domain.mail.entity.MailLog;
import com.example.cs25entity.domain.mail.entity.QMailLog;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class MailLogCustomRepositoryImpl implements MailLogCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public Page<MailLog> search(MailLogSearchDto condition, Pageable pageable) {
        QMailLog mailLog = QMailLog.mailLog;

        BooleanBuilder builder = new BooleanBuilder();

        if (condition.getMailStatus() != null) {
            builder.and(mailLog.status.eq(condition.getMailStatus()));
        }

        if (condition.getStartDate() != null) {
            builder.and(mailLog.sendDate.goe(condition.getStartDate().atStartOfDay()));
        }

        if (condition.getEndDate() != null) {
            builder.and(mailLog.sendDate.loe(condition.getEndDate().atTime(LocalTime.MAX)));
        }

        List<MailLog> content = queryFactory
            .selectFrom(mailLog)
            .where(builder)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(mailLog.sendDate.desc())
            .fetch();

        Long total = queryFactory
            .select(mailLog.count())
            .from(mailLog)
            .where(builder)
            .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }
}
