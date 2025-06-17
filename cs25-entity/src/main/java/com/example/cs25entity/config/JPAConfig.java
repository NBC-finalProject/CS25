package com.example.cs25entity.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration// 공통 모듈의 entity, repository, component를 인식하기 위한 스캔 설정
@EntityScan(basePackages = {
    "com.example.cs25common.domain",
    "com.example.cs25entity.domain"
})
@EnableJpaRepositories(basePackages =
    "com.example.cs25entity.domain"
)
@ComponentScan(basePackages = {
    "com.example.cs25entity",
    "com.example.cs25common"            // 공통 모듈
})
public class JPAConfig {
    // 추가적인 JPA 설정이 필요하면 여기에 추가
}
