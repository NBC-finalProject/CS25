package com.example.cs25service.ai;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import com.example.cs25service.domain.ai.client.ClaudeChatClient;
import com.example.cs25service.domain.ai.client.FallbackAiChatClient;
import com.example.cs25service.domain.ai.client.OpenAiChatClient;
import com.example.cs25service.domain.ai.service.AiFeedbackStreamWorker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
public class FallbackAiChatClientTest {

    @Test
    void openAiFail_thenFallbackToClaude() {
        // given
        OpenAiChatClient openAiMock = mock(OpenAiChatClient.class);
        ClaudeChatClient claudeMock = mock(ClaudeChatClient.class);

        // OpenAI는 실패하도록 설정
        when(openAiMock.call(anyString(), anyString()))
            .thenThrow(new RuntimeException("OpenAI failure"));

        // Claude는 정상 반환
        when(claudeMock.call(anyString(), anyString()))
            .thenReturn("Claude 응답입니다.");

        FallbackAiChatClient fallbackClient = new FallbackAiChatClient(openAiMock, claudeMock);

        // when
        String result = fallbackClient.call("시스템 프롬프트", "유저 프롬프트");

        // then
        assertThat(result).isEqualTo("Claude 응답입니다.");
        verify(openAiMock, times(1)).call(anyString(), anyString());
        verify(claudeMock, times(1)).call(anyString(), anyString());
    }

}