package com.example.cs25.batch.jobs;

import com.example.cs25.domain.mail.dto.MailDto;
import com.example.cs25.domain.mail.service.MailService;
import com.example.cs25.domain.quiz.service.TodayQuizService;
import com.example.cs25.domain.subscription.dto.SubscriptionMailTargetDto;
import com.example.cs25.domain.subscription.dto.SubscriptionRequest;
import com.example.cs25.domain.subscription.entity.DayOfWeek;
import com.example.cs25.domain.subscription.entity.SubscriptionPeriod;
import com.example.cs25.domain.subscription.service.SubscriptionService;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class DailyMailSendJob {

    private final SubscriptionService subscriptionService;
    private final TodayQuizService todayQuizService;
    private final MailService mailService;

    @Bean
    public Job mailJob(JobRepository jobRepository,
        @Qualifier("mailStep") Step mailStep,
        @Qualifier("mailConsumeStep") Step mailConsumeStep) {
        return new JobBuilder("mailJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(mailStep)
            .next(mailConsumeStep)
            .build();
    }

    @Bean
    public Step mailStep(JobRepository jobRepository,
        @Qualifier("mailTasklet") Tasklet mailTasklet,
        PlatformTransactionManager transactionManager) {
        return new StepBuilder("mailStep", jobRepository)
            .tasklet(mailTasklet, transactionManager)
            .build();
    }

    @Bean
    public Step mailConsumeStep(
        JobRepository jobRepository,
        @Qualifier("redisStreamReader") ItemReader<Map<String, String>> reader,
        @Qualifier("mailMessageProcessor") ItemProcessor<Map<String, String>, MailDto> processor,
        @Qualifier("mailWriter") ItemWriter<MailDto> writer,
        PlatformTransactionManager transactionManager
    ) {
        return new StepBuilder("mailConsumeStep", jobRepository)
            .<Map<String, String>, MailDto>chunk(10, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .build();
    }

    // TODO: Chunk 방식 고려
    @Bean
    public Tasklet mailTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[배치 시작] 구독자 대상 메일 발송");
            // FIXME: Fake Subscription
//            Set<DayOfWeek> fakeDays = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
//                DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
//            SubscriptionRequest fakeRequest = SubscriptionRequest.builder()
//                .period(SubscriptionPeriod.ONE_MONTH)
//                .email("wannabeing@123.123")
//                .isActive(true)
//                .days(fakeDays)
//                .category("BACKEND")
//                .build();
//            subscriptionService.createSubscription(fakeRequest);

            List<SubscriptionMailTargetDto> subscriptions = subscriptionService.getTodaySubscriptions();

            for (SubscriptionMailTargetDto sub : subscriptions) {
                Long subscriptionId = sub.getSubscriptionId();
                String email = sub.getEmail();

                // Today 퀴즈 발송
                todayQuizService.issueTodayQuiz(subscriptionId);

                log.info("메일 전송 대상: {} -> quiz {}", email, 0);
            }

            log.info("[배치 종료] 메일 발송 완료");
            return RepeatStatus.FINISHED;
        };
    }

}
