package com.digitalearn.npaxis.student;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * DTO for filtering students.
 */
@Getter
@Setter
public class StudentFilter {

    private String university;
    private String program;
    private String graduationYear;
    private String phone;

    // optional advanced filter
    private List<Long> savedPreceptorIds;
}