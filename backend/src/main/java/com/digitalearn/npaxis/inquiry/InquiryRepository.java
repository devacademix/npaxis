package com.digitalearn.npaxis.inquiry;

import com.digitalearn.npaxis.auditing.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface InquiryRepository extends BaseRepository<Inquiry, Long> {
    Page<Inquiry> findByPreceptor_User_UserId(Long preceptorId, Pageable pageable);

    Page<Inquiry> findByPreceptor_User_UserIdAndStatus(
            Long preceptorId,
            InquiryStatus status,
            Pageable pageable
    );

    Page<Inquiry> findByStudent_User_UserId(Long preceptorId, Pageable pageable);

    Page<Inquiry> findByStudent_User_UserIdAndStatus(
            Long preceptorId,
            InquiryStatus status,
            Pageable pageable
    );
}
