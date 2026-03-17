package com.digitalearn.npaxis.exceptions;

import com.digitalearn.npaxis.common.responses.DateTimeUtils;
import com.digitalearn.npaxis.exceptionhandler.ExceptionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {

        log.warn("Access denied for request [{} {}]: {}",
                request.getMethod(),
                request.getRequestURI(),
                accessDeniedException.getMessage());

        ExceptionResponse errorResponse = ExceptionResponse.builder()
                .error("INSUFFICIENT_PERMISSIONS")
                .businessErrorDescription(accessDeniedException.getMessage())
                .businessErrorCode(HttpStatus.FORBIDDEN.value())
                .timestamp(DateTimeUtils.localDateTimeToString(LocalDateTime.now()))
                .build();

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");

        response.getWriter()
                .write(objectMapper.writeValueAsString(errorResponse));
    }
}