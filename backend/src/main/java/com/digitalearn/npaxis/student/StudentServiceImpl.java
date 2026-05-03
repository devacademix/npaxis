package com.digitalearn.npaxis.student;

import com.digitalearn.npaxis.analytics.EventType;
import com.digitalearn.npaxis.analytics.TrackEvent;
import com.digitalearn.npaxis.exceptions.ResourceNotFoundException;
import com.digitalearn.npaxis.preceptor.Preceptor;
import com.digitalearn.npaxis.preceptor.PreceptorMapper;
import com.digitalearn.npaxis.preceptor.PreceptorRepository;
import com.digitalearn.npaxis.preceptor.PreceptorResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of StudentService.
 * <p>
 * ============================================
 * ANALYTICS TRACKING
 * ============================================
 * This service tracks student activity:
 * - SEARCH_PERFORMED: student searches for other students
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    private final PreceptorRepository preceptorRepository;
    private final PreceptorMapper preceptorMapper;

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponseDTO> getAllActiveStudents() {
        log.debug("Student Service Impl --> Get all active students");
        return studentRepository.findAllActive().stream()
                .map(studentMapper::toStudentDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    @TrackEvent(
            eventType = EventType.SEARCH_PERFORMED,
            metadataExpression = "{'resultCount': #result.getNumberOfElements(), 'pageNumber': #pageable.getPageNumber(), 'pageSize': #pageable.getPageSize()}"
    )
    public Page<StudentResponseDTO> searchStudents(
            StudentFilter filter,
            Pageable pageable
    ) {
        Specification<Student> spec = this.buildStudentSpec(filter);
        Page<Student> page = studentRepository.findAll(spec, pageable);
        return page.map(studentMapper::toStudentDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponseDTO getActiveStudentById(Long userId) {
        log.debug("Student Service Impl --> Get active student by ID: {}", userId);
        Student student = studentRepository.findActiveById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + userId));
        return studentMapper.toStudentDTO(student);
    }

    @Override
    @Transactional
    public StudentResponseDTO updateStudent(Long userId, StudentRequestDTO studentRequestDto) {
        log.debug("Student Service Impl --> Update student by ID: {}", userId);
        Student existingStudent = studentRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + userId));

        // Map updates from DTO to existing entity
        Student updatedStudent = studentMapper.toStudentEntity(studentRequestDto);
        updatedStudent.setUserId(userId);
        updatedStudent.setUser(existingStudent.getUser());
        updatedStudent.setSavedPreceptors(existingStudent.getSavedPreceptors());

        Student savedStudent = studentRepository.save(updatedStudent);
        return studentMapper.toStudentDTO(savedStudent);
    }

    @Override
    @Transactional
    public void softDeleteStudent(Long userId) {
        log.debug("Student Service Impl --> Soft delete student by ID: {}", userId);
        studentRepository.softDelete(userId);
    }

    @Override
    @Transactional
    public void hardDeleteStudent(Long userId) {
        log.debug("Student Service Impl --> Hard delete student by ID: {}", userId);
        studentRepository.hardDelete(userId);
    }

    @Override
    @Transactional
    public void restoreStudent(Long userId) {
        log.debug("Student Service Impl --> Restore student by ID: {}", userId);
        studentRepository.restore(userId);
    }

    @Override
    @Transactional
    public void savePreceptor(Long userId, Long preceptorId) {
        log.debug("Student Service Impl --> Save preceptor: {} for student: {}", preceptorId, userId);
        Student student = studentRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + userId));
        Preceptor preceptor = preceptorRepository.findById(preceptorId)
                .orElseThrow(() -> new ResourceNotFoundException("Preceptor not found with ID: " + preceptorId));

        student.getSavedPreceptors().add(preceptor);
        studentRepository.save(student);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PreceptorResponseDTO> getSavedPreceptors(Long userId) {
        log.debug("Student Service Impl --> Get saved preceptors for student: {}", userId);
        Student student = studentRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + userId));
        return student.getSavedPreceptors().stream()
                .map(preceptorMapper::toPreceptorDTO)
                .toList();
    }

    private Specification<Student> buildStudentSpec(StudentFilter filter) {
        return Specification
                .where(StudentSpecification.isNotDeleted())
                .and(StudentSpecification.isActive())
                .and(StudentSpecification.hasUniversity(filter.getUniversity()))
                .and(StudentSpecification.hasProgram(filter.getProgram()))
                .and(StudentSpecification.hasGraduationYear(filter.getGraduationYear()))
                .and(StudentSpecification.hasPhone(filter.getPhone()))
                .and(StudentSpecification.hasSavedPreceptors(filter.getSavedPreceptorIds()));
    }
}
