package com.digitalearn.npaxis.auth;


import com.digitalearn.npaxis.user.User;

public interface RegistrationStrategy {

    boolean supports(Long roleId);

    // The actual registration logic
    void createProfile(User savedUser, BaseRegistrationRequest request);
}