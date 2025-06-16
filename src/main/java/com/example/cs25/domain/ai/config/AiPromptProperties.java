package com.example.cs25.domain.ai.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
@ConfigurationProperties(prefix = "ai.prompt")
public class AiPromptProperties {

    private Feedback feedback = new Feedback();
    private Generation generation = new Generation();

    @Getter
    public static class Feedback {

        private String system;
        private String user;

        public void setSystem(String system) {
            this.system = system;
        }

        public void setUser(String user) {
            this.user = user;
        }
    }

    @Getter
    public static class Generation {

        private String topicSystem;
        private String topicUser;
        private String categorySystem;
        private String categoryUser;
        private String generateSystem;
        private String generateUser;

        public void setTopicSystem(String s) {
            this.topicSystem = s;
        }

        public void setTopicUser(String s) {
            this.topicUser = s;
        }

        public void setCategorySystem(String s) {
            this.categorySystem = s;
        }

        public void setCategoryUser(String s) {
            this.categoryUser = s;
        }

        public void setGenerateSystem(String s) {
            this.generateSystem = s;
        }

        public void setGenerateUser(String s) {
            this.generateUser = s;
        }
    }

    public void setFeedback(Feedback feedback) {
        this.feedback = feedback;
    }

    public void setGeneration(Generation generation) {
        this.generation = generation;
    }
}
