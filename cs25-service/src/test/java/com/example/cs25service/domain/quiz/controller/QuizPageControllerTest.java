package com.example.cs25service.domain.quiz.controller;

import static com.example.cs25entity.domain.quiz.enums.QuizFormatType.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.example.cs25entity.domain.quiz.enums.QuizLevel;
import com.example.cs25service.domain.quiz.dto.QuizCategoryResponseDto;
import com.example.cs25service.domain.quiz.dto.TodayQuizResponseDto;
import com.example.cs25service.domain.quiz.service.QuizPageService;
import com.example.cs25service.domain.security.jwt.provider.JwtTokenProvider;

@ActiveProfiles("test")
@WebMvcTest(QuizPageController.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // DB 설정이 그대로 사용됨 (application-test.properties 기반)
class QuizPageControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private QuizPageService quizPageService;

	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;

	@Test
	@DisplayName("오늘의 문제를 가져오기")
	@WithMockUser("wannabeing")
	void showTodayQuizPage() throws Exception {
	    // given
		TodayQuizResponseDto responseDto = TodayQuizResponseDto.builder()
			.question("오늘의 문제")
			.choice1("1. 카리나")
			.choice2("2. 장원영")
			.choice3("3. 설윤")
			.choice4("4. 지원")
			.answerNumber("4개 모두 정답")
			.commentary("고를 수 없다.")
			.quizType(MULTIPLE_CHOICE.name())
			.quizLevel(QuizLevel.HARD.name())
			.category(QuizCategoryResponseDto.builder().main("BACKEND").sub("GIRL").build())
			.build();

		given(quizPageService.showTodayQuizPage(anyString()))
			.willReturn(responseDto);

	    // when & then
		mockMvc.perform(MockMvcRequestBuilders
				.get("/todayQuiz?subscriptionId={subscriptionId}&quizId={quizId}", "subId", "quizId")
				.with(csrf()))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.httpCode").value(200))
			.andExpect(jsonPath("$.data.question").value(responseDto.getQuestion()))
			.andExpect(jsonPath("$.data.answerNumber").value(responseDto.getAnswerNumber()))
			.andExpect(jsonPath("$.data.quizType").value(responseDto.getQuizType()));
	}
}