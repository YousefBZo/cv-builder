package com.yousef.cvbuilder.social.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EducationSection {

    private String institution;
    private String fieldOfStudy;
    private String degreeLevel;
    private String graduationYear;
}
