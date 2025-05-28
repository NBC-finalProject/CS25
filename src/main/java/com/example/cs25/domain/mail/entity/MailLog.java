package com.example.cs25.domain.mail.entity;

import com.example.cs25.domain.mail.enums.MailStatus;
import com.example.cs25.domain.quiz.entity.Quiz;
import com.example.cs25.domain.users.entity.User;
import jakarta.persistence.Entity;
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
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    private LocalDateTime sendDate;

    private MailStatus status;

    @Builder
    public MailLog(Long id, User user, Quiz quiz, LocalDateTime sendDate, MailStatus status) {
        this.id = id;
        this.user = user;
        this.quiz = quiz;
        this.sendDate = sendDate;
        this.status = status;
    }

    public void updateMailStatus(MailStatus status) {
        this.status = status;
    }
}
