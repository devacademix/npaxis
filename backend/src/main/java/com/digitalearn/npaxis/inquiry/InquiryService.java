package com.digitalearn.npaxis.inquiry;

import com.digitalearn.npaxis.user.User;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

public interface InquiryService {
    InquiryResponseDTO sendInquiry(Long studentId, InquiryRequestDTO requestDTO);

    Page<InquiryResponseDTO> getUserEnquiries(
            User user,
            InquiryStatus status,
            Pageable pageable
    );

    void markAsRead(Long enquiryId);
}
