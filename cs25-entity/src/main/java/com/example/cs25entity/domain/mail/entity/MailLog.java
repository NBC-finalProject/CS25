package com.example.cs25entity.domain.mail.entity;

import com.example.cs25entity.domain.mail.enums.MailStatus;
import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "mail_logs")
@NoArgsConstructor
public class MailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    private LocalDateTime sendDate;

    @Enumerated(EnumType.STRING)
    private MailStatus status;

    /**
     * Constructs a MailLog entity with the specified id, user, quiz, send date, and mail status.
     *
     * @param id           the unique identifier for the mail log entry
     * @param subscription the user associated with the mail log
     * @param quiz         the quiz associated with the mail log
     * @param sendDate     the date and time the mail was sent
     * @param status       the status of the mail
     */
    @Builder
    public MailLog(Long id, Subscription subscription, Quiz quiz, LocalDateTime sendDate,
        MailStatus status) {
        this.id = id;
        this.subscription = subscription;
        this.quiz = quiz;
        this.sendDate = sendDate;
        this.status = status;
    }

    /**
     * Updates the mail status for this log entry.
     *
     * @param status the new mail status to set
     */
    public void updateMailStatus(MailStatus status) {
        this.status = status;
    }
}
