package com.example.cs25entity.domain.subscription.repository;


import com.example.cs25entity.domain.subscription.entity.SubscriptionHistory;
import com.example.cs25entity.domain.subscription.exception.SubscriptionHistoryException;
import com.example.cs25entity.domain.subscription.exception.SubscriptionHistoryExceptionCode;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionHistoryRepository extends JpaRepository<SubscriptionHistory, Long> {

    default SubscriptionHistory findByIdOrElseThrow(Long subscriptionHistoryId) {
        return findById(subscriptionHistoryId)
            .orElseThrow(() ->
                new SubscriptionHistoryException(
                    SubscriptionHistoryExceptionCode.NOT_FOUND_SUBSCRIPTION_HISTORY_ERROR));
    }

    List<SubscriptionHistory> findAllBySubscriptionId(Long subscriptionId);
}
