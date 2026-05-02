package com.digitalearn.npaxis.auth;

import com.digitalearn.npaxis.validation.ValidPhone;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StudentRegistrationRequest extends BaseRegistrationRequest {
    @NotBlank(message = "University is required")
    @Size(max = 100, message = "University cannot exceed 100 characters")
    private String university;

    @NotBlank(message = "Program is required")
    @Size(max = 100, message = "Program cannot exceed 100 characters")
    private String program;

    @NotBlank(message = "Graduation year is required")
    @Size(min = 4, max = 4, message = "Graduation year must be exactly 4 digits")
    private String graduationYear;

    @ValidPhone
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phone;
}