package com.digitalearn.npaxis.inquiry;

import com.digitalearn.npaxis.analytics.AnalyticsEventRequest;
import com.digitalearn.npaxis.analytics.AnalyticsService;
import com.digitalearn.npaxis.analytics.EventType;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class InquiryServiceImpl implements InquiryService {

    private final InquiryRepository inquiryRepository;
    private final PreceptorRepository preceptorRepository;
    private final StudentRepository studentRepository;
    private final InquiryMapper inquiryMapper;
    private final EmailService emailService;
    private final AnalyticsService analyticsService;


    @Override
    @Transactional
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

        // Log Analytics
        logAnalyticsEvent(preceptor.getUserId());

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

    private void logAnalyticsEvent(Long preceptorId) {
        AnalyticsEventRequest analyticsRequest = new AnalyticsEventRequest(EventType.INQUIRY, preceptorId);
        analyticsService.logEvent(analyticsRequest);
    }
}
