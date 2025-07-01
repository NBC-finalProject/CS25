package com.example.cs25entity.domain.subscription.repository;

import com.example.cs25entity.domain.subscription.dto.SubscriptionMailTargetDto;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25entity.domain.subscription.exception.SubscriptionException;
import com.example.cs25entity.domain.subscription.exception.SubscriptionExceptionCode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    boolean existsByEmail(String email);

    @Query("SELECT s FROM Subscription s JOIN FETCH s.category WHERE s.id = :id")
    Optional<Subscription> findByIdWithCategory(Long id);

    default Subscription findByIdOrElseThrow(Long subscriptionId) {
        return findById(subscriptionId)
            .orElseThrow(() ->
                new SubscriptionException(SubscriptionExceptionCode.NOT_FOUND_SUBSCRIPTION_ERROR));
    }

    @Query(value = """
        SELECT
            s.id AS subscriptionId,
            s.email AS email,
            c.category_type AS category
        FROM subscription s
        JOIN quiz_category c ON s.quiz_category_id = c.id
        WHERE s.is_active = true
          AND s.start_date <= :today
          AND s.end_date >= :today
          AND (s.subscription_type & :todayBit) != 0
        """, nativeQuery = true)
    List<SubscriptionMailTargetDto> findAllTodaySubscriptions(
        @Param("today") LocalDate today,
        @Param("todayBit") int todayBit);

    Optional<Subscription> findByEmail(String email);

    Page<Subscription> findAllByOrderByIdAsc(Pageable pageable);

    Optional<Subscription> findBySerialId(String serialId);

    default Subscription findBySerialIdOrElseThrow(String subscriptionId) {
        return findBySerialId(subscriptionId)
            .orElseThrow(() ->
                new SubscriptionException(SubscriptionExceptionCode.NOT_FOUND_SUBSCRIPTION_ERROR));
    }

}
