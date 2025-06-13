package com.example.cs25.global.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class ErrorResponseUtil {

    public static void writeJsonError(HttpServletResponse response, int statusCode, String message)
        throws IOException {

        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("code", statusCode);
        errorBody.put("status", HttpStatus.valueOf(statusCode).name());
        errorBody.put("message", message);

        String json = new ObjectMapper().writeValueAsString(errorBody);
        response.getWriter().write(json);
    }
}
