package com.example.cs25.domain.subscription.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.cs25.domain.subscription.entity.SubscriptionLog;
import com.example.cs25.domain.subscription.exception.SubscriptionLogException;
import com.example.cs25.domain.subscription.exception.SubscriptionLogExceptionCode;

public interface SubscriptionLogRepository extends JpaRepository<SubscriptionLog, Long> {
	default SubscriptionLog findByIdOrElseThrow(Long subscriptionLogId){
		return findById(subscriptionLogId)
			.orElseThrow(() ->
				new SubscriptionLogException(SubscriptionLogExceptionCode.NOT_FOUND_SUBSCRIPTION_LOG_ERROR));
	}
}
