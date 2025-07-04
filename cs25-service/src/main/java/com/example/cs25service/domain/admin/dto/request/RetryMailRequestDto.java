package com.example.cs25service.domain.admin.dto.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RetryMailRequestDto {
    List<Long> subscriptionIds;
}
