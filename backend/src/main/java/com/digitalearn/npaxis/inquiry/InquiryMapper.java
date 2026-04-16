package com.digitalearn.npaxis.inquiry;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class InquiryMapper {

    @Mapping(target = "studentName", source = "student.user.displayName")
    public abstract InquiryResponseDTO toResponseDTO(Inquiry inquiry);

    public abstract Inquiry toEntity(InquiryRequestDTO requestDTO);
}
