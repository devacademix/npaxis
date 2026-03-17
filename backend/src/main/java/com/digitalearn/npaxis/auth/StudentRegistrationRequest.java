package com.digitalearn.npaxis.auth;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StudentRegistrationRequest extends BaseRegistrationRequest {
    private String university;
    private String program;
    private String graduationYear;
    private String phone;
}