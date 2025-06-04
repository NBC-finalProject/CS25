package com.example.cs25.domain.users.service;

import com.example.cs25.domain.users.dto.UserProfileResponse;
import com.example.cs25.domain.users.repository.UserRepository;
import com.example.cs25.global.dto.AuthUser;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserProfileResponse getUserProfile(AuthUser authUser) {

        return UserProfileResponse.builder().build();
    }

    private String createCode() {
        int length = 6;
        Random random;

        try {
            random = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) { //SecureRandom.getInstanceStrong()에서 사용하는 알고리즘을 JVM 에서 지원하지 않을 때
            random = new SecureRandom();
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(random.nextInt(10));
        }

        return builder.toString();
    }
}
