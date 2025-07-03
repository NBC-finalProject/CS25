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

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

//    @Query("SELECT u FROM User u LEFT JOIN FETCH u.subscription WHERE u.email = :email")
//    Optional<User> findUserWithSubscriptionByEmail(String email);

    default Optional<User> validateSocialJoinEmail(String email, SocialType socialType) {
        return findByEmail(email)
            .filter(existingUser -> existingUser.getSocialType().equals(socialType));
    }

    Optional<User> findBySubscription(Subscription subscription);

    default User findByIdOrElseThrow(Long id) {
        return findById(id).orElseThrow(() -> new UserException(UserExceptionCode.NOT_FOUND_USER));
    }

    Page<User> findAllByOrderByIdAsc(Pageable pageable);

    @Query("SELECT COUNT(u) + 1 FROM User u WHERE u.score > :score")
    int findRankByScore(double score);

    Optional<User> findBySerialId(String serialId);

    default User findBySerialIdOrElseThrow(String serialId) {
        return findBySerialId(serialId)
            .orElseThrow(() -> new UserException(UserExceptionCode.NOT_FOUND_USER));
    }

    boolean existsByEmail(String email);
}
