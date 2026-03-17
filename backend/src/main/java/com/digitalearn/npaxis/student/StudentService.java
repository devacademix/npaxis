package com.digitalearn.npaxis.student;

import jakarta.validation.Valid;

import java.util.List;

/**
 * Service interface for managing Student details.
 */
public interface StudentService {

    /**
     * Retrieves all active students.
     *
     * @return List of StudentResponseDTOs.
     */
    List<StudentResponseDTO> getAllActiveStudents();

    /**
     * Retrieves an active student by their ID.
     *
     * @param userId The ID of the student.
     * @return StudentResponseDTO.
     */
    StudentResponseDTO getActiveStudentById(Long userId);

    /**
     * Updates an existing student's details.
     *
     * @param userId            The ID of the student.
     * @param studentRequestDto Updated details.
     * @return Updated StudentResponseDTO.
     */
    StudentResponseDTO updateStudent(Long userId, @Valid StudentRequestDTO studentRequestDto);

    /**
     * Soft deletes a student.
     *
     * @param userId The ID of the student.
     */
    void softDeleteStudent(Long userId);

    /**
     * Hard deletes a student.
     *
     * @param userId The ID of the student.
     */
    void hardDeleteStudent(Long userId);

    /**
     * Restores a soft-deleted student.
     *
     * @param userId The ID of the student.
     */
    void restoreStudent(Long userId);

    /**
     * Saves a preceptor to student's bookmarks.
     *
     * @param userId      The ID of the student.
     * @param preceptorId The ID of the preceptor.
     */
    void savePreceptor(Long userId, Long preceptorId);

    /**
     * Retrieves all saved preceptors for a student.
     *
     * @param userId The ID of the student.
     * @return List of PreceptorResponseDTOs.
     */
    List<com.digitalearn.npaxis.preceptor.PreceptorResponseDTO> getSavedPreceptors(Long userId);
}
