package com.example.cs25.domain.users.entity;

import com.example.cs25.domain.users.vo.Subscription;
import com.example.cs25.global.entity.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
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

    private int totalSolved;

    @Embedded
    private Subscription subscription;

    @Builder
    public User(String email, String name){
        this.email = email;
        this.name = name;
        totalSolved = 0;
    }

    public void updateEmail(String email){
        this.email = email;
    }

    public void updateName(String name){
        this.name = name;
    }
}
