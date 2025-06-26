package com.example.cs25service.domain.subscription.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.EnumSet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import com.example.cs25entity.domain.subscription.entity.DayOfWeek;
import com.example.cs25service.domain.security.dto.AuthUser;
import com.example.cs25service.domain.security.jwt.provider.JwtTokenProvider;
import com.example.cs25service.domain.subscription.dto.SubscriptionInfoDto;
import com.example.cs25service.domain.subscription.dto.SubscriptionRequestDto;
import com.example.cs25service.domain.subscription.dto.SubscriptionResponseDto;
import com.example.cs25service.domain.subscription.service.SubscriptionService;

@ActiveProfiles("test")
@WebMvcTest(SubscriptionController.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // DB 설정이 그대로 사용됨 (application-test.properties 기반)
class SubscriptionControllerTest {
	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	SubscriptionService subscriptionService;

	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;

	@Test
	@DisplayName("구독ID로 구독정보 가져오기")
	@WithMockUser(username = "wannabeing")
	void getSubscription_success() throws Exception {
	    // given
		SubscriptionInfoDto responseDto = SubscriptionInfoDto.builder()
			.category("BACKEND")
			.email("123@123.com")
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusMonths(1))
			.days(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY)) // 월,화,수
			.active(true)
			.period(1) // 1개월
			.build();

		given(subscriptionService.getSubscription(anyString()))
			.willReturn(responseDto);

		// when & then
		mockMvc.perform(MockMvcRequestBuilders
				.get("/subscriptions/{subscriptionId}", "id")
				.with(csrf()))
			.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.httpCode").value(200))
				.andExpect(jsonPath("$.data.category").value("BACKEND"))
				.andExpect(jsonPath("$.data.email").value("123@123.com"))
				.andExpect(jsonPath("$.data.active").value(true))
				.andExpect(jsonPath("$.data.period").value(1L));
	}

	@Test
	@DisplayName("구독정보 생성하기")
	@WithMockUser(username = "wannabeing")
	void createSubscription_true() throws Exception {
	    // given
		SubscriptionResponseDto responseDto = SubscriptionResponseDto.builder()
			.id(1L)
			.category("BACKEND")
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusMonths(1))
			.subscriptionType(127) // 주7일 구독
			.build();

		given(subscriptionService
			.createSubscription(any(SubscriptionRequestDto.class), any()))
			.willReturn(responseDto);

		// when & then
		mockMvc.perform(MockMvcRequestBuilders
				.post("/subscriptions")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
						"category":"BACKEND",
						"email": "123@123.com",
						"period":1,
						"days":["MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY","SUNDAY"]
					}
					""")
				.with(csrf()))
			.andDo(print())
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.httpCode").value(201))
			.andExpect(jsonPath("$.data.id").value(1L))
			.andExpect(jsonPath("$.data.category").value("BACKEND"))
			.andExpect(jsonPath("$.data.subscriptionType").value(127));
	}

	@Test
	@DisplayName("구독정보 업데이트하기")
	@WithMockUser(username = "wannabeing")
	void updateSubscription_success() throws Exception {
		// given
		doNothing()
			.when(subscriptionService)
			.updateSubscription(anyString(), any(SubscriptionRequestDto.class));

		// when & then
		mockMvc.perform(MockMvcRequestBuilders
				.patch("/subscriptions/{subscriptionId}", "id")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
						"category":"BACKEND",
						"email": "123@123.com",
						"period":1,
						"active": false,
						"days":["MONDAY"]
					}
					""")
				.with(csrf()))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.httpCode").value(200));
	}

	@Test
	@DisplayName("구독 취소하기")
	@WithMockUser(username = "wannabeing")
	void cancelSubscription_success() throws Exception {
	    // given
		doNothing()
			.when(subscriptionService)
			.cancelSubscription(anyString());

	    // when & then
		mockMvc.perform(MockMvcRequestBuilders
				.patch("/subscriptions/{subscriptionId}/cancel", "id")
				.with(csrf()))
			.andDo(print())
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("이메일 체크하기")
	@WithMockUser(username = "wannabeing")
	void checkEmail_success() throws Exception {
	    // given
		doNothing()
			.when(subscriptionService)
			.checkEmail(anyString());

		// when & then
		mockMvc.perform(MockMvcRequestBuilders
				.get("/subscriptions/email/check" )
				.param("email", "123@123.com")
				.with(csrf()))
			.andDo(print())
			.andExpect(status().isOk());
	}
}