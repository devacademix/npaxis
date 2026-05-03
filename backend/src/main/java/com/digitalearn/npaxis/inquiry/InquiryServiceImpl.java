package com.digitalearn.npaxis.inquiry;

import com.digitalearn.npaxis.analytics.EventType;
import com.digitalearn.npaxis.analytics.TrackEvent;
import com.digitalearn.npaxis.email.EmailService;
import com.digitalearn.npaxis.email.EmailTemplate;
import com.digitalearn.npaxis.exceptionhandler.BusinessErrorCodes;
import com.digitalearn.npaxis.exceptions.BusinessException;
import com.digitalearn.npaxis.exceptions.ResourceNotFoundException;
import com.digitalearn.npaxis.preceptor.Preceptor;
import com.digitalearn.npaxis.preceptor.PreceptorRepository;
import com.digitalearn.npaxis.role.RoleName;
import com.digitalearn.npaxis.student.Student;
import com.digitalearn.npaxis.student.StudentRepository;
import com.digitalearn.npaxis.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Implementation of InquiryService.
 * <p>
 * ============================================
 * ANALYTICS TRACKING
 * ============================================
 * <p>
 * This service is instrumented with @TrackEvent annotations to automatically
 * capture student-to-preceptor inquiry events:
 * <p>
 * - INQUIRY_SUBMITTED: when a student submits an inquiry to a preceptor
 * <p>
 * Events are tracked asynchronously without blocking business logic.
 * The targetId is the preceptor ID for context about who received the inquiry.
 *
 * @see TrackEvent
 * @see EventType
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InquiryServiceImpl implements InquiryService {

    private final InquiryRepository inquiryRepository;
    private final PreceptorRepository preceptorRepository;
    private final StudentRepository studentRepository;
    private final InquiryMapper inquiryMapper;
    private final EmailService emailService;


    /**
     * Sends an inquiry from a student to a preceptor.
     * <p>
     * ANALYTICS:
     * - Tracks INQUIRY_SUBMITTED event when inquiry is successfully created
     * - targetId is the preceptor ID
     * - Metadata includes inquiry subject for context
     */
    @Override
    @Transactional
    @TrackEvent(
            eventType = EventType.INQUIRY_SUBMITTED,
            targetIdExpression = "#requestDTO.preceptorId().toString()",
            metadataExpression = "{'preceptorId': #requestDTO.preceptorId(), " +
                    "'studentId': #studentId, " +
                    "'inquiryStatus': #result.status}"
    )
    public InquiryResponseDTO sendInquiry(Long studentId, InquiryRequestDTO requestDTO) {
        log.info("InquiryServiceImpl --> Sending inquiry from student ID {} to preceptor ID {}", studentId, requestDTO.preceptorId());

        Preceptor preceptor = preceptorRepository.findById(requestDTO.preceptorId())
                .orElseThrow(() -> new ResourceNotFoundException("Preceptor not found with ID: " + requestDTO.preceptorId()));

        if (!preceptor.isPremium()) {
            throw new BusinessException(BusinessErrorCodes.PRECEPTOR_NOT_PREMIUM);
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

        Inquiry inquiry = inquiryMapper.toEntity(requestDTO);
        inquiry.setStudent(student);
        inquiry.setPreceptor(preceptor);
        inquiry.setStatus(InquiryStatus.NEW);
        inquiry = inquiryRepository.save(inquiry);

        // Send Async Email
        sendInquiryEmail(preceptor, student, inquiry.getMessage());


        return inquiryMapper.toResponseDTO(inquiry);
    }

    @Override
    public Page<InquiryResponseDTO> getUserEnquiries(
            User currentUser,
            InquiryStatus status,
            Pageable pageable
    ) {

        Page<Inquiry> page;

        if (currentUser.getRole().getRoleName() == RoleName.ROLE_STUDENT) {
            page = (status == null)
                    ? inquiryRepository.findByStudent_User_UserId(currentUser.getUserId(), pageable)
                    : inquiryRepository.findByStudent_User_UserIdAndStatus(currentUser.getUserId(), status, pageable);

        } else if (currentUser.getRole().getRoleName() == RoleName.ROLE_PRECEPTOR) {
            // Verify that the preceptor is premium before allowing them to view inquiries
            Preceptor preceptor = preceptorRepository.findById(currentUser.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Preceptor not found with ID: " + currentUser.getUserId()));

            if (!preceptor.isPremium()) {
                throw new BusinessException(BusinessErrorCodes.PRECEPTOR_NOT_PREMIUM);
            }

            page = (status == null)
                    ? inquiryRepository.findByPreceptor_User_UserId(currentUser.getUserId(), pageable)
                    : inquiryRepository.findByPreceptor_User_UserIdAndStatus(currentUser.getUserId(), status, pageable);

        } else {
            throw new IllegalStateException("Unsupported role");
        }

        return page.map(inquiryMapper::toResponseDTO);
    }


    @Override
    public void markAsRead(Long enquiryId) {

        Inquiry enquiry = inquiryRepository.findById(enquiryId)
                .orElseThrow(() -> new RuntimeException("Inquiry not found"));

        enquiry.setStatus(InquiryStatus.READ);
        inquiryRepository.save(enquiry);
    }

    private void sendInquiryEmail(Preceptor preceptor, Student student, String message) {
        emailService.sendEmail(
                preceptor.getUser().getEmail(),
                EmailTemplate.INQUIRY_EMAIL,
                Map.of(
                        "preceptorName", preceptor.getUser().getName() != null ? preceptor.getUser().getName() : preceptor.getUser().getDisplayName(),
                        "studentName", student.getUser().getDisplayName(),
                        "message", message
                ));
    }
}
