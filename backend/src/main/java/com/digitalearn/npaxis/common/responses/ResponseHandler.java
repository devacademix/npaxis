package com.digitalearn.npaxis.common.responses;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

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
     * Generates a standardized API response.
     *
     * @param data       The data to be included in the response.
     * @param message    A message describing the response.
     * @param isSuccess  A boolean indicating whether the operation was successful.
     * @param httpStatus The HTTP status code for the response.
     * @param <T>        The type of the data being returned.
     * @return A ResponseEntity containing the standardized response.
     */
    public static <T> ResponseEntity<Map<String, Object>> generateResponse(
            T data,
            String message,
            Boolean isSuccess,
            HttpStatus httpStatus
    ) {


        Map<String, Object> response = new LinkedHashMap<>();
        response.put("data", data);
        response.put("message", message);
        response.put("isSuccess", isSuccess);
        response.put("statusCode", httpStatus.value());
        response.put("timestamp", DateTimeUtils.localDateTimeToString(LocalDateTime.now()));

        return new ResponseEntity<>(response, httpStatus);
    }
}
