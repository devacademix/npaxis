package com.digitalearn.npaxis.common.responses;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

/**
 * Utility class to handle API responses.
 * This class provides a method to generate a standardized response format for API endpoints.
 */
public class ResponseHandler {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ResponseHandler() {
        /* This utility class should not be instantiated */
    }


    /**
     * For non-paginated response
     */
    public static <T> ResponseEntity<GenericApiResponse<T>> generateResponse(T data, String message, Boolean isSuccess, HttpStatus httpStatus) {

        return ResponseEntity.ok(
                GenericApiResponse.<T>builder()
                        .data(data)
                        .message(message)
                        .isSuccess(isSuccess)
                        .statusCode(httpStatus)
                        .timestamp(DateTimeUtils.localDateTimeToString(LocalDateTime.now()))
                        .build()
        );
    }

    /**
     * For paginated response
     */
    public static <T> ResponseEntity<GenericApiResponse<T>> generatePaginatedResponse(Page<?> page, T data, String message, Boolean isSuccess, HttpStatus httpStatus) {

        return ResponseEntity.ok(
                GenericApiResponse.<T>builder()
                        .data(data)
                        .isSuccess(true)
                        .message(message)
                        .isSuccess(isSuccess)
                        .statusCode(httpStatus)
                        .timestamp(DateTimeUtils.localDateTimeToString(LocalDateTime.now()))
                        .meta(GenericApiResponse.Meta.builder()
                                .totalElements(page.getTotalElements())
                                .totalPages(page.getTotalPages())
                                .page(page.getNumber())
                                .size(page.getSize())
                                .build())
                        .build()
        );
    }
}
