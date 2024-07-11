package com.weatherapi.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleRateLimitExceededException() {
        RateLimitExceededException ex = new RateLimitExceededException("Rate limit exceeded");
        ProblemDetail problemDetail = handler.handleRateLimitExceededException(ex);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), problemDetail.getStatus());
        assertEquals("Rate Limit Exceeded", problemDetail.getTitle());
        assertEquals("Rate limit exceeded", problemDetail.getDetail());
    }

    @Test
    void handleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid input");
        ProblemDetail problemDetail = handler.handleIllegalArgumentException(ex);

        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        assertEquals("Invalid Input", problemDetail.getTitle());
        assertEquals("Invalid input", problemDetail.getDetail());
    }

    @Test
    void handleGenericException() {
        Exception ex = new Exception("Unexpected error");
        ProblemDetail problemDetail = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problemDetail.getStatus());
        assertEquals("Internal Server Error", problemDetail.getTitle());
        assertEquals("An unexpected error occurred", problemDetail.getDetail());
    }


}

