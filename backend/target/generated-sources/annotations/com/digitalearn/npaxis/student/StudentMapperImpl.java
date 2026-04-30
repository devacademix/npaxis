package com.digitalearn.npaxis.student;

import com.digitalearn.npaxis.user.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-30T19:47:51+0530",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class StudentMapperImpl extends StudentMapper {

    @Override
    public StudentResponseDTO toStudentDTO(Student student) {
        if ( student == null ) {
            return null;
        }

        String displayName = null;
        String email = null;
        Long userId = null;
        String university = null;
        String program = null;
        String graduationYear = null;
        String phone = null;

        displayName = studentUserDisplayName( student );
        email = studentUserEmail( student );
        userId = student.getUserId();
        university = student.getUniversity();
        program = student.getProgram();
        graduationYear = student.getGraduationYear();
        phone = student.getPhone();

        StudentResponseDTO studentResponseDTO = new StudentResponseDTO( userId, displayName, email, university, program, graduationYear, phone );

        return studentResponseDTO;
    }

    @Override
    public Student toStudentEntity(StudentRequestDTO studentRequestDto) {
        if ( studentRequestDto == null ) {
            return null;
        }

        Student.StudentBuilder<?, ?> student = Student.builder();

        student.graduationYear( studentRequestDto.graduationYear() );
        student.phone( studentRequestDto.phone() );
        student.program( studentRequestDto.program() );
        student.university( studentRequestDto.university() );

        return student.build();
    }

    private String studentUserDisplayName(Student student) {
        User user = student.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getDisplayName();
    }

    private String studentUserEmail(Student student) {
        User user = student.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getEmail();
    }
}
