package com.digitalearn.npaxis.inquiry;

import java.time.LocalDateTime;

public record InquiryResponseDTO(
        Long inquiryId,
        String studentName,
        String subject,
        String message,
        InquiryStatus status,
        LocalDateTime createdAt
) {
}
