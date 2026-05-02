package com.digitalearn.npaxis.auth;

import com.digitalearn.npaxis.validation.ValidPhone;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PreceptorRegistrationRequest extends BaseRegistrationRequest {
    /**
     * List of credential names (case-insensitive).
     * Example: ["MBBS", "MD"]
     */
    @Size(max = 10, message = "A preceptor can have at most 10 credentials")
    private List<String> credentials;

    /**
     * List of specialty names (case-insensitive).
     * Example: ["Cardiology", "Internal Medicine"]
     * At least one specialty is required.
     */
    @NotBlank(message = "At least one specialty is required")
    @Size(max = 5, message = "A preceptor can have at most 5 specialties")
    private List<String> specialties;

    @NotBlank(message = "Location is required")
    @Size(max = 150, message = "Location cannot exceed 150 characters")
    private String location;

    @ValidPhone
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phone;
}