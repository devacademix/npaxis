package com.digitalearn.npaxis.student;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper class for converting Student entities to Student DTOs and vice-versa.
 */
@Mapper(componentModel = "spring")
public abstract class StudentMapper {

    // Entity to DTO mapping
    @Mapping(target = "displayName", source = "user.displayName")
    @Mapping(target = "email", source = "user.email")
    public abstract StudentResponseDTO toStudentDTO(Student student);

    // DTO to Entity mapping
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "savedPreceptors", ignore = true)
    public abstract Student toStudentEntity(StudentRequestDTO studentRequestDto);
}
