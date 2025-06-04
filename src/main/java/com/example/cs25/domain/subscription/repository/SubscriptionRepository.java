package com.example.cs25.domain.subscription.repository;

import com.example.cs25.domain.subscription.entity.Subscription;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByEmail(String email);
}
