package com.digitalearn.npaxis.common.responses;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
public class GenericApiResponse<T> {
    private T data;
    private String message;
    private Boolean isSuccess;
    private HttpStatus statusCode;
    private String timestamp;
    private Meta meta;

    /**
     * Pagination metadata.
     */
    @Getter
    @Builder
    public static class Meta {
        private Long totalElements;
        private Integer totalPages;
        private Integer page;
        private Integer size;
    }
}
