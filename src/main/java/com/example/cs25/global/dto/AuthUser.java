package com.example.cs25.global.dto;

import com.example.cs25.domain.users.entity.Role;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import com.example.cs25.domain.users.entity.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.example.cs25.domain.users.entity.User;

@Builder
@Getter
@RequiredArgsConstructor
public class AuthUser implements OAuth2User {
    private final Long id;
    private final String email;
    private final String name;
    private final Role role;

    public AuthUser(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.role = user.getRole();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }

    // TODO: 유저역할이 나뉘면 수정해야하는 메서드
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}
