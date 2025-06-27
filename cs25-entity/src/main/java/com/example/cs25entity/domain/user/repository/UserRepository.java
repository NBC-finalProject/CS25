package com.example.cs25entity.domain.user.repository;


import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25entity.domain.user.entity.SocialType;
import com.example.cs25entity.domain.user.entity.User;
import com.example.cs25entity.domain.user.exception.UserException;
import com.example.cs25entity.domain.user.exception.UserExceptionCode;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.subscription WHERE u.email = :email")
    Optional<User> findUserWithSubscriptionByEmail(String email);

    default void validateSocialJoinEmail(String email, SocialType socialType) {
        findByEmail(email).ifPresent(existingUser -> {
            if (!existingUser.getSocialType().equals(socialType)) {
                throw new UserException(UserExceptionCode.EMAIL_DUPLICATION);
            }
        });
    }

    Optional<User> findBySubscription(Subscription subscription);

    default User findByIdOrElseThrow(Long id) {
        return findById(id).orElseThrow(() -> new UserException(UserExceptionCode.NOT_FOUND_USER));
    }

    Page<User> findAllByOrderByIdAsc(Pageable pageable);

    @Query("SELECT COUNT(u) + 1 FROM User u WHERE u.score > :score")
    int findRankByScore(double score);

    Optional<User> findBySerialId(String serialId);

    boolean existsByEmail(String email);
}
