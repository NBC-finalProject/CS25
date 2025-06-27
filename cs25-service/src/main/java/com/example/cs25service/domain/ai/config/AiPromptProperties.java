package com.example.cs25service.domain.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ai.prompt")
public class AiPromptProperties {

    private Feedback feedback = new Feedback();
    private Generation generation = new Generation();
    private Keyword keyword = new Keyword();

    @Getter
    @Setter
    public static class Feedback {

        private String system;
        private String user;
    }

    @Getter
    @Setter
    public static class Generation {

        private String topicSystem;
        private String topicUser;
        private String categorySystem;
        private String categoryUser;
        private String generateSystem;
        private String generateUser;
    }

    @Getter
    @Setter
    public static class Keyword {

        private String system;
        private String user;
    }
}
