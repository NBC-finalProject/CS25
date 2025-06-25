package com.example.cs25service.domain.profile.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.cs25entity.domain.quiz.repository.QuizCategoryRepository;
import com.example.cs25entity.domain.subscription.repository.SubscriptionHistoryRepository;
import com.example.cs25entity.domain.user.repository.UserRepository;
import com.example.cs25entity.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import com.example.cs25service.domain.subscription.service.SubscriptionService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @InjectMocks
    private ProfileService profileService;

    @Mock
    private UserQuizAnswerRepository userQuizAnswerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SubscriptionService subscriptionService;
    @Mock
    private SubscriptionHistoryRepository subscriptionHistoryRepository;
    @Mock
    private QuizCategoryRepository quizCategoryRepository;

}