package com.digitalearn.npaxis.auth;


import com.digitalearn.npaxis.preceptor.CredentialService;
import com.digitalearn.npaxis.preceptor.Preceptor;
import com.digitalearn.npaxis.preceptor.PreceptorRepository;
import com.digitalearn.npaxis.preceptor.SpecialtyService;
import com.digitalearn.npaxis.role.RoleRepository;
import com.digitalearn.npaxis.user.User;
import com.digitalearn.npaxis.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class PreceptorRegistrationStrategy implements RegistrationStrategy {

    private final UserRepository userRepository;
    private final PreceptorRepository preceptorRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CredentialService credentialService;
    private final SpecialtyService specialtyService;

    @Override
    public boolean supports(Long roleId) {
        return roleId != null && roleId.equals(2L); // Assuming 2 is Preceptor ID
    }

    @Override
    @Transactional
    public void createProfile(User savedUser, BaseRegistrationRequest request) {
        PreceptorRegistrationRequest preceptorReq = (PreceptorRegistrationRequest) request;

        Preceptor preceptor = new Preceptor();
        preceptor.setUser(savedUser);

        // Convert specialty list to Specialty entities
        if (preceptorReq.getSpecialties() != null && !preceptorReq.getSpecialties().isEmpty()) {
            preceptor.setSpecialties(specialtyService.getOrCreateSpecialties(
                    new HashSet<>(preceptorReq.getSpecialties())));
        }

        // Convert credential list to Credential entities
        if (preceptorReq.getCredentials() != null && !preceptorReq.getCredentials().isEmpty()) {
            preceptor.setCredentials(credentialService.getOrCreateCredentials(
                    new HashSet<>(preceptorReq.getCredentials())));
        }

        preceptor.setLocation(preceptorReq.getLocation());
        preceptor.setPhone(preceptorReq.getPhone());

        preceptorRepository.save(preceptor);
    }
}