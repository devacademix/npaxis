package com.digitalearn.npaxis.auth;

import com.digitalearn.npaxis.role.RoleRepository;
import com.digitalearn.npaxis.student.Student;
import com.digitalearn.npaxis.student.StudentRepository;
import com.digitalearn.npaxis.user.User;
import com.digitalearn.npaxis.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentRegistrationStrategy implements RegistrationStrategy {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean supports(Long roleId) {
        return roleId != null && roleId.equals(1L); // Assuming 1 is Student ID
    }

    @Override
    @Transactional
    public void createProfile(User savedUser, BaseRegistrationRequest request) {
        StudentRegistrationRequest studentReq = (StudentRegistrationRequest) request;

        Student student = new Student();
        student.setUser(savedUser); // Link the profile to the saved User record
        student.setUniversity(studentReq.getUniversity());
        student.setProgram(studentReq.getProgram());
        student.setGraduationYear(studentReq.getGraduationYear());
        student.setPhone(studentReq.getPhone());

        studentRepository.save(student);
    }
}