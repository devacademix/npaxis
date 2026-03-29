package com.digitalearn.npaxis.admin;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO for top performing preceptors.
 */
@Getter
@Builder
public class TopPreceptorDTO {

    private Long preceptorId;
    private String name;
    private Long inquiries;
}