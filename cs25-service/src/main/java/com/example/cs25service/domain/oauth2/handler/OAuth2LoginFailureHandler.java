package com.example.cs25service.domain.oauth2.handler;

import com.example.cs25entity.domain.user.exception.UserException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Value("${FRONT_END_URI:http://localhost:5173}")
    private String frontEndUri;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception) throws IOException, ServletException {
// 예: UserException이 내부에 wrap 되어 있을 수 있음

        Throwable cause = exception.getCause();

        if (cause instanceof UserException userException) {
            // 1. 응답 발생시키기
            request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 500);
            request.setAttribute(RequestDispatcher.ERROR_MESSAGE, userException.getMessage());
            request.setAttribute("exception", userException);

            // 2. Spring의 에러 처리로 넘기기 (→ templates/error/500.html 로 이동)
            request.getRequestDispatcher("/error").forward(request, response);

        } else {
            // 알 수 없는 오류는 외부 리디렉션
            response.sendRedirect(frontEndUri);
        }
    }
}
