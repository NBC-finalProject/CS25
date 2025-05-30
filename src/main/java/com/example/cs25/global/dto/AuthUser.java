package com.example.cs25.global.dto;

import com.example.cs25.domain.users.entity.Role;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Builder
@Getter
@RequiredArgsConstructor
public class AuthUser implements OAuth2User {
    private final Long id;
    private final String email;
    private final String name;
    private final Role role;

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }

    //이거 유저역할 추가되면 추가해야함
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getName() {
        return name;
    }
}
