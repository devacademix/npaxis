package com.digitalearn.npaxis.inquiry;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record InquiryRequestDTO(

        @NotNull(message = "Preceptor ID is mandatory")
        Long preceptorId,
        String subject,
        @NotBlank(message = "Message is mandatory")
        String message
) {
}
