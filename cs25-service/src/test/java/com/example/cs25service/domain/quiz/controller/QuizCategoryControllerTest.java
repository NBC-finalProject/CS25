package com.example.cs25service.domain.quiz.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;

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

import com.example.cs25service.domain.quiz.service.QuizCategoryService;
import com.example.cs25service.domain.security.jwt.provider.JwtTokenProvider;

@ActiveProfiles("test")
@WebMvcTest(QuizCategoryController.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // DB 설정이 그대로 사용됨 (application-test.properties 기반)
class QuizCategoryControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private QuizCategoryService quizCategoryService;

	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;

	@Test
	@DisplayName("퀴즈 카테고리를 가져오기")
	@WithMockUser(username = "wannabeing")
	void getQuizCategories() throws Exception {
	    // given
		List<String> categories = new ArrayList<>();
		categories.add("BACKEND");
		categories.add("FRONTEND");
		categories.add("CS");

		given(quizCategoryService.getParentQuizCategoryList())
			.willReturn(categories);

	    // when & then
		mockMvc.perform(MockMvcRequestBuilders
				.get("/quiz-categories")
				.with(csrf()))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.httpCode").value(200))
			.andExpect(jsonPath("$.data").value(categories));
	}

}