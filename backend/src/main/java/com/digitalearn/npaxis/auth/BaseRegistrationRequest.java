package com.digitalearn.npaxis.auth;

import com.digitalearn.npaxis.validation.ValidPassword;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "roleId",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = StudentRegistrationRequest.class, name = "1"),
        @JsonSubTypes.Type(value = PreceptorRegistrationRequest.class, name = "2")
})
public abstract class BaseRegistrationRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be a valid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @ValidPassword
    private String password;

    @NotBlank(message = "Display name is required")
    private String displayName;

    // The ID of the role in the database (1 = Student, 2 = Preceptor)
    private Long roleId;
}