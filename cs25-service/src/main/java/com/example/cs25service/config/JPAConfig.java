package com.example.cs25service.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration("jpaConfigFromService")
@ComponentScan(basePackages = {
    "com.example.cs25service",          // 자기 자신
    "com.example.cs25common",
    "com.example.cs25entity"// 공통 모듈
})
public class JPAConfig {
    // 추가적인 JPA 설정이 필요하면 여기에 추가
}
