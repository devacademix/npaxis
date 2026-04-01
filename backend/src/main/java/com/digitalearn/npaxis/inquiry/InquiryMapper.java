package com.digitalearn.npaxis.inquiry;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InquiryMapper {

    @Mapping(target = "studentName", source = "student.user.displayName")
    InquiryResponseDTO toResponseDTO(Inquiry inquiry);

    Inquiry toEntity(InquiryRequestDTO requestDTO);
}
