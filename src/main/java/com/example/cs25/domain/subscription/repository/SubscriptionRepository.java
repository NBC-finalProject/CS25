package com.example.cs25.domain.subscription.repository;

import com.example.cs25.domain.subscription.entity.Subscription;
import com.example.cs25.domain.subscription.exception.SubscriptionException;
import com.example.cs25.domain.subscription.exception.SubscriptionExceptionCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    boolean existsByEmail(String email);

    @Query("SELECT s FROM Subscription s JOIN FETCH s.category WHERE s.id = :id")
    Optional<Subscription> findByIdWithCategory(Long id);

    default Subscription findByIdOrElseThrow(Long subscriptionId) {
        return findById(subscriptionId)
            .orElseThrow(() ->
                new SubscriptionException(SubscriptionExceptionCode.NOT_FOUND_SUBSCRIPTION_ERROR));
    }
}
