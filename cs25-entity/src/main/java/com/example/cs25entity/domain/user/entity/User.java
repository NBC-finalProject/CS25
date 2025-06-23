package com.example.cs25entity.domain.user.entity;

import com.example.cs25common.global.entity.BaseEntity;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String name;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    private boolean isActive = true;

    @Enumerated(EnumType.STRING)
    private Role role;

    private double score = 0;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @Column(unique = true)
    private String serialId;

    /**
     * Constructs a new User with the specified email and name, initializing totalSolved to zero.
     *
     * @param email the user's email address
     * @param name  the user's name
     */
    @Builder
    public User(String email, String name, SocialType socialType, Role role, double score,
        Subscription subscription) {
        this.email = email;
        this.name = name;
        this.socialType = socialType;
        this.role = role;
        this.score = score;
        this.subscription = subscription;
    }

    /****
     * Updates the user's email address.
     *
     * @param email the new email address to set
     */
    public void updateEmail(String email) {
        this.email = email;
    }

    /****
     * Updates the user's name.
     *
     * @param name the new name to set for the user
     */
    public void updateName(String name) {
        this.name = name;
    }

    public void updateDisableUser() {
        this.isActive = false;
    }

    public void updateEnableUser() {
        this.isActive = true;
    }

    public void updateSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public void updateScore(double score) {
        this.score = score;
    }
}
