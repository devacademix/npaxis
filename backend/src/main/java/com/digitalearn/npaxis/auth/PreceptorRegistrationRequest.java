package com.digitalearn.npaxis.auth;

import com.digitalearn.npaxis.validation.ValidPhone;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PreceptorRegistrationRequest extends BaseRegistrationRequest {
    @Size(max = 255, message = "Credentials cannot exceed 255 characters")
    private String credentials;

    @NotBlank(message = "Specialty is required")
    @Size(max = 100, message = "Specialty cannot exceed 100 characters")
    private String specialty;

    @NotBlank(message = "Location is required")
    @Size(max = 150, message = "Location cannot exceed 150 characters")
    private String location;

    @ValidPhone
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phone;
}