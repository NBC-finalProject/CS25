package com.example.cs25.domain.subscription.repository;

import com.example.cs25.domain.subscription.entity.SubscriptionLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionLogRepository extends JpaRepository<SubscriptionLog, Long> {

    List<SubscriptionLog> findAllBySubscriptionId(Long subscriptionId);
}
