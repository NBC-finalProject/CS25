package com.example.cs25.domain.subscription.repository;

import com.example.cs25.domain.subscription.entity.Subscription;
import com.example.cs25.domain.subscription.exception.SubscriptionException;
import com.example.cs25.domain.subscription.exception.SubscriptionExceptionCode;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
	boolean existsByEmail(String email);

	default Subscription findByIdOrElseThrow(Long subscriptionId){
		return findById(subscriptionId)
			.orElseThrow(() ->
				new SubscriptionException(SubscriptionExceptionCode.NOT_FOUND_SUBSCRIPTION_ERROR));
	}
}
