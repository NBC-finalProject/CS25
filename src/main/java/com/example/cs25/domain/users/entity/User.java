package com.example.cs25.domain.users.entity;

import com.example.cs25.domain.oauth.dto.SocialType;
import com.example.cs25.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "users")
public class User extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String name;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    private boolean isActive = true;

    @Enumerated(EnumType.STRING)
    private Role role;


    /**
     * Constructs a new User with the specified email and name, initializing totalSolved to zero.
     *
     * @param email the user's email address
     * @param name the user's name
     */
    @Builder
    public User(String email, String name, SocialType socialType, Role role){
        this.email = email;
        this.name = name;
        this.socialType = socialType;
        this.role = role;
    }

    /****
     * Updates the user's email address.
     *
     * @param email the new email address to set
     */
    public void updateEmail(String email){
        this.email = email;
    }

    /****
     * Updates the user's name.
     *
     * @param name the new name to set for the user
     */
    public void updateName(String name){
        this.name = name;
    }

    public void updateActive(boolean isActive){
        this.isActive = isActive;
    }

}
