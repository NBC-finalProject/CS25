package com.example.cs25.domain.subscription.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.cs25.domain.subscription.entity.SubscriptionHistory;
import com.example.cs25.domain.subscription.exception.SubscriptionHistoryException;
import com.example.cs25.domain.subscription.exception.SubscriptionHistoryExceptionCode;

public interface SubscriptionHistoryRepository extends JpaRepository<SubscriptionHistory, Long> {
	default SubscriptionHistory findByIdOrElseThrow(Long subscriptionHistoryId){
		return findById(subscriptionHistoryId)
			.orElseThrow(() ->
				new SubscriptionHistoryException(SubscriptionHistoryExceptionCode.NOT_FOUND_SUBSCRIPTION_HISTORY_ERROR));
	}
}
