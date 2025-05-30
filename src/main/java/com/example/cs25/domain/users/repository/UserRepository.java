package com.example.cs25.domain.users.repository;

import com.example.cs25.domain.users.entity.SocialType;
import com.example.cs25.domain.users.entity.User;
import com.example.cs25.domain.users.exception.UserException;
import com.example.cs25.domain.users.exception.UserExceptionCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    default void validateSocialJoinEmail(String email, SocialType socialType) {
        findByEmail(email).ifPresent(existingUser -> {
            if (!existingUser.getSocialType().equals(socialType)) {
                throw new UserException(UserExceptionCode.EMAIL_DUPLICATION);
            }
        });
    }
}
