package com.digitalearn.npaxis.admin;

import com.digitalearn.npaxis.admin.dto.AdminStudentDetailDTO;
import com.digitalearn.npaxis.admin.dto.AdminStudentListDTO;
import com.digitalearn.npaxis.common.responses.GenericApiResponse;
import com.digitalearn.npaxis.common.responses.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.digitalearn.npaxis.utils.APIConstants.ADMINISTRATION_API;
import static com.digitalearn.npaxis.utils.APIConstants.ADMIN_STUDENTS_LIST_API;
import static com.digitalearn.npaxis.utils.APIConstants.ADMIN_STUDENTS_SEARCH_API;
import static com.digitalearn.npaxis.utils.APIConstants.ADMIN_STUDENT_DETAIL_API;
import static com.digitalearn.npaxis.utils.APIConstants.ADMIN_STUDENT_UPDATE_API;
import static com.digitalearn.npaxis.utils.APIConstants.BASE_API;

/**
 * Admin controller for student management operations
 */
@RestController
@RequestMapping(BASE_API + "/" + ADMINISTRATION_API)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Student Management", description = "Admin-only APIs for student management")
public class AdminStudentController {

    private final AdminService adminService;

    @Operation(summary = "List all students (admin view)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(ADMIN_STUDENTS_LIST_API)
    public ResponseEntity<GenericApiResponse<List<AdminStudentListDTO>>> listStudents(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Admin listing all students");
        Page<AdminStudentListDTO> students = adminService.listAllStudents(pageable);
        return ResponseHandler.generatePaginatedResponse(students, students.getContent(),
                "Students fetched successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Search and filter students (admin view)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(ADMIN_STUDENTS_SEARCH_API)
    public ResponseEntity<GenericApiResponse<List<AdminStudentListDTO>>> searchStudents(
            @RequestParam(required = false) String university,
            @RequestParam(required = false) String program,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Admin searching students");
        Page<AdminStudentListDTO> students = adminService.searchStudents(university, program, pageable);
        return ResponseHandler.generatePaginatedResponse(students, students.getContent(),
                "Students found successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Get student detail (admin view)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(ADMIN_STUDENT_DETAIL_API)
    public ResponseEntity<GenericApiResponse<AdminStudentDetailDTO>> getStudentDetail(
            @PathVariable Long userId) {
        log.info("Admin fetching student detail - userId: {}", userId);
        AdminStudentDetailDTO student = adminService.getStudentDetailAsAdmin(userId);
        return ResponseHandler.generateResponse(student, "Student detail fetched successfully",
                true, HttpStatus.OK);
    }

    @Operation(summary = "Update student (admin)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(ADMIN_STUDENT_UPDATE_API)
    public ResponseEntity<GenericApiResponse<AdminStudentDetailDTO>> updateStudent(
            @PathVariable Long userId,
            @Valid @RequestBody AdminStudentDetailDTO updateDTO) {
        log.info("Admin updating student - userId: {}", userId);
        AdminStudentDetailDTO updated = adminService.updateStudentAsAdmin(userId, updateDTO);
        return ResponseHandler.generateResponse(updated, "Student updated successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Delete student (soft delete)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(ADMIN_STUDENT_UPDATE_API)
    public ResponseEntity<GenericApiResponse<String>> deleteStudent(@PathVariable Long userId) {
        log.info("Admin deleting student - userId: {}", userId);
        // Future: implement soft delete for student
        return ResponseHandler.generateResponse(null, "Student deleted successfully", true, HttpStatus.OK);
    }

    @Operation(summary = "Get student inquiries")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(ADMIN_STUDENT_DETAIL_API + "/inquiries")
    public ResponseEntity<GenericApiResponse<List<?>>> getStudentInquiries(
            @PathVariable Long userId) {
        log.info("Admin fetching inquiries for student - userId: {}", userId);
        // Future: implement getStudentInquiriesAsAdmin
        return ResponseHandler.generateResponse(null, "Inquiries fetched successfully", true, HttpStatus.OK);
    }
}
