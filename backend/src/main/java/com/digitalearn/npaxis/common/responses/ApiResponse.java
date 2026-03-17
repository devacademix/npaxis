package com.digitalearn.npaxis.common.responses;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private T data;
    private String message;
    private Boolean isSuccess;
    private Integer statusCode;
    private String timestamp;
}
