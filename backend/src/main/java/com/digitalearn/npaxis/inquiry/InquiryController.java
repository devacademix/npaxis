package com.digitalearn.npaxis.inquiry;

import com.digitalearn.npaxis.common.responses.GenericApiResponse;
import com.digitalearn.npaxis.common.responses.ResponseHandler;
import com.digitalearn.npaxis.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.digitalearn.npaxis.utils.APIConstants.GET_INQUIRIES_FOR_USER;
import static com.digitalearn.npaxis.utils.APIConstants.INQUIRIES_BASE_API;
import static com.digitalearn.npaxis.utils.APIConstants.MARK_INQUIRY_AS_READ;
import static com.digitalearn.npaxis.utils.APIConstants.SEND_INQUIRY_API;

@Slf4j
@RestController
@RequestMapping(INQUIRIES_BASE_API)
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    @PostMapping(SEND_INQUIRY_API)
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<GenericApiResponse<InquiryResponseDTO>> sendInquiry(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody InquiryRequestDTO requestDTO) {
        log.info("InquiryController --> Received inquiry request from user ID: {}", user.getUserId());
        return ResponseHandler.generateResponse(inquiryService.sendInquiry(user.getUserId(), requestDTO), "Inquiry Sent successfully.", true, HttpStatus.OK);
    }

    @GetMapping(value = {GET_INQUIRIES_FOR_USER, GET_INQUIRIES_FOR_USER + "/"})
    @PreAuthorize("hasAnyRole('STUDENT', 'PRECEPTOR')")
    public ResponseEntity<GenericApiResponse<List<InquiryResponseDTO>>> getPreceptorInquiries(
            @AuthenticationPrincipal User user,
            @RequestParam InquiryStatus inquiryStatus,
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {

        Page<InquiryResponseDTO> preceptorEnquiries = inquiryService.getUserEnquiries(user, inquiryStatus, pageable);

        return ResponseHandler.generatePaginatedResponse(preceptorEnquiries, preceptorEnquiries.getContent(), "Preceptor Inquiries fetched successfully", true, HttpStatus.OK);
    }

    @PatchMapping(value = {MARK_INQUIRY_AS_READ, MARK_INQUIRY_AS_READ + "/"})
    public ResponseEntity<GenericApiResponse<Void>> markAsRead(@PathVariable Long inquiryId) {
        this.inquiryService.markAsRead(inquiryId);
        return ResponseHandler.generateResponse(null, "Marked as read", true, HttpStatus.OK);
    }

}
