package com.example.cs25.domain.users.repository;

import com.example.cs25.domain.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
