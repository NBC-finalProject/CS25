package com.example.cs25.domain.oauth2.dto;

import java.util.Map;

import com.example.cs25.domain.oauth2.exception.OAuth2Exception;
import com.example.cs25.domain.oauth2.exception.OAuth2ExceptionCode;

/**
 * @author choihyuk
 *
 * OAuth2 소셜 응답 클래스들의 공통 메서드를 포함한 추상 클래스
 * 자식 클래스에서 유틸 메서드(castOrThrow 등)를 사용할 수 있습니다.
 */
public abstract class AbstractOAuth2Response implements OAuth2Response {
	/**
	 * 소셜 로그인에서 제공받은 데이터를 Map 형태로 형변환하는 메서드
	 * @param attributes 소셜에서 제공 받은 데이터
	 * @return 형변환된 Map 데이터를 반환
	 */
	@SuppressWarnings("unchecked")
	Map<String, Object> castOrThrow(Object attributes) {
		if(!(attributes instanceof Map)) {
			throw new OAuth2Exception(OAuth2ExceptionCode.SOCIAL_ATTRIBUTES_PARSING_FAILED);
		}
		return (Map<String, Object>) attributes;
	}
}
