package com.digitalearn.npaxis.auth;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PreceptorRegistrationRequest extends BaseRegistrationRequest {
    private String credentials;
    private String specialty;
    private String location;
    private String phone;
}