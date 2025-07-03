package com.example.cs25service.domain.oauth2.handler;

import com.example.cs25entity.domain.user.exception.UserException;
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
            response.setStatus(userException.getHttpStatus().value());
            response.setContentType("application/json;charset=UTF-8");

            String json = """
                {
                    "httpCode": "%s",
                    "message": "%s"
                }
                """.formatted(userException.getErrorCode(), userException.getMessage());

            response.getWriter().write(json);

            //ErrorResponseUtil.writeJsonError(response, 500,
            //    userException.getMessage());
            //response.sendRedirect("http://localhost:5173");
        } else {
            // 알 수 없는 오류
            response.sendRedirect("http://localhost:5173");
        }
    }
}
