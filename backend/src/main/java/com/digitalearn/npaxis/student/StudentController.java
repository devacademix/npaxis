package com.digitalearn.npaxis.student;

import com.digitalearn.npaxis.common.responses.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static com.digitalearn.npaxis.utils.APIConstants.GET_ACTIVE_STUDENT_BY_ID_API;
import static com.digitalearn.npaxis.utils.APIConstants.GET_ALL_ACTIVE_STUDENTS_API;
import static com.digitalearn.npaxis.utils.APIConstants.GET_SAVED_PRECEPTORS_API;
import static com.digitalearn.npaxis.utils.APIConstants.HARD_DELETE_STUDENT_BY_ID_API;
import static com.digitalearn.npaxis.utils.APIConstants.PUT_UPDATE_STUDENT_API;
import static com.digitalearn.npaxis.utils.APIConstants.RESTORE_STUDENT_BY_ID_API;
import static com.digitalearn.npaxis.utils.APIConstants.SAVE_PRECEPTOR_API;
import static com.digitalearn.npaxis.utils.APIConstants.SOFT_DELETE_STUDENT_BY_ID_API;
import static com.digitalearn.npaxis.utils.APIConstants.STUDENTS_API;
import com.digitalearn.npaxis.preceptor.PreceptorResponseDTO;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping(STUDENTS_API)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Student Management", description = "APIs for managing student details.")
public class StudentController {

    private final StudentService studentService;

    @Operation(summary = "Fetch all active students", description = "Retrieves a list of all active students.")
    @GetMapping(value = {"", "/", GET_ALL_ACTIVE_STUDENTS_API, GET_ALL_ACTIVE_STUDENTS_API + "/"})
    public ResponseEntity<Map<String, Object>> getAllActiveStudents() {
        log.info("Fetching all active students");
        List<StudentResponseDTO> students = studentService.getAllActiveStudents();
        return ResponseHandler.generateResponse(students, "Students fetched successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Fetch student by ID", description = "Retrieves an active student by their unique user ID.")
    @GetMapping(value = {GET_ACTIVE_STUDENT_BY_ID_API, GET_ACTIVE_STUDENT_BY_ID_API + "/"})
    public ResponseEntity<Map<String, Object>> getActiveStudentById(@PathVariable("userId") Long userId) {
        log.info("Fetching active student with ID: {}", userId);
        StudentResponseDTO student = studentService.getActiveStudentById(userId);
        return ResponseHandler.generateResponse(student, "Student fetched successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Update student details", description = "Updates the details of an existing student.")
    @PreAuthorize("#userId == principal.userId")
    @PutMapping(value = {PUT_UPDATE_STUDENT_API, PUT_UPDATE_STUDENT_API + "/"})
    public ResponseEntity<Map<String, Object>> updateStudent(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody StudentRequestDTO studentRequestDto) {
        log.info("Updating student with ID: {}", userId);
        StudentResponseDTO updatedStudent = studentService.updateStudent(userId, studentRequestDto);
        return ResponseHandler.generateResponse(updatedStudent, "Student updated successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Soft delete student", description = "Deactivates a student by their unique user ID.")
    @PreAuthorize("hasRole('ADMIN') or #userId == principal.userId")
    @DeleteMapping(value = {SOFT_DELETE_STUDENT_BY_ID_API, SOFT_DELETE_STUDENT_BY_ID_API + "/"})
    public ResponseEntity<Map<String, Object>> softDeleteStudent(@PathVariable("userId") Long userId) {
        log.info("Soft deleting student with ID: {}", userId);
        studentService.softDeleteStudent(userId);
        return ResponseHandler.generateResponse(null, "Student deactivated successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Hard delete student", description = "Permanently deletes a student by their unique user ID.")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(value = {HARD_DELETE_STUDENT_BY_ID_API, HARD_DELETE_STUDENT_BY_ID_API + "/"})
    public ResponseEntity<Map<String, Object>> hardDeleteStudent(@PathVariable("userId") Long userId) {
        log.info("Hard deleting student with ID: {}", userId);
        studentService.hardDeleteStudent(userId);
        return ResponseHandler.generateResponse(null, "Student deleted permanently", true, HttpStatus.OK);
    }

    @Operation(summary = "Restore student", description = "Restores a soft-deleted student.")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = {RESTORE_STUDENT_BY_ID_API, RESTORE_STUDENT_BY_ID_API + "/"})
    public ResponseEntity<Map<String, Object>> restoreStudent(@PathVariable("userId") Long userId) {
        log.info("Restoring student with ID: {}", userId);
        studentService.restoreStudent(userId);
        return ResponseHandler.generateResponse(null, "Student restored successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Save preceptor to bookmarks", description = "Saves a preceptor to student's bookmarks.")
    @PreAuthorize("#userId == principal.userId")
    @PostMapping(value = {SAVE_PRECEPTOR_API, SAVE_PRECEPTOR_API + "/"})
    public ResponseEntity<Map<String, Object>> savePreceptor(
            @PathVariable("userId") Long userId,
            @PathVariable("preceptorId") Long preceptorId) {
        log.info("Saving preceptor {} for student {}", preceptorId, userId);
        studentService.savePreceptor(userId, preceptorId);
        return ResponseHandler.generateResponse(null, "Preceptor bookmarked successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Get all saved preceptors", description = "Retrieves all preceptors saved by the student.")
    @PreAuthorize("#userId == principal.userId")
    @GetMapping(value = {GET_SAVED_PRECEPTORS_API, GET_SAVED_PRECEPTORS_API + "/"})
    public ResponseEntity<Map<String, Object>> getSavedPreceptors(@PathVariable("userId") Long userId) {
        log.info("Fetching saved preceptors for student ID: {}", userId);
        List<PreceptorResponseDTO> savedPreceptors = studentService.getSavedPreceptors(userId);
        return ResponseHandler.generateResponse(savedPreceptors, "Saved preceptors fetched successfully", true, HttpStatus.OK);
    }
}
