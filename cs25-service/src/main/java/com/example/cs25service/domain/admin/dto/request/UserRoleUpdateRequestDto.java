package com.example.cs25service.domain.admin.dto.request;

import com.example.cs25entity.domain.user.entity.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserRoleUpdateRequestDto {

    private Role role;

}
