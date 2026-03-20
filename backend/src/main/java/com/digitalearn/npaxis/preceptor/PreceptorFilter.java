package com.digitalearn.npaxis.preceptor;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * DTO for filtering preceptors.
 */
@Getter
@Setter
public class PreceptorFilter {

    private String specialty;
    private String location;
    private List<DayOfWeekEnum> availableDays;
    private Integer minHonorarium;
    private Integer maxHonorarium;
}