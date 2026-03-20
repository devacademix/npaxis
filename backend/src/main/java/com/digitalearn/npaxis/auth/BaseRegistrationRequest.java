package com.digitalearn.npaxis.auth;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
    private String email;
    private String password;
    private String displayName;

    // The ID of the role in the database (1 = Student, 2 = Preceptor)
    private Long roleId;
}