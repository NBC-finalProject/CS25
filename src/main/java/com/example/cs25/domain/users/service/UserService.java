package com.example.cs25.domain.users.service;

import com.example.cs25.domain.users.dto.UserProfileResponse;
import com.example.cs25.domain.users.repository.UserRepository;
import com.example.cs25.global.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserProfileResponse getUserProfile(AuthUser authUser) {

    }
}
