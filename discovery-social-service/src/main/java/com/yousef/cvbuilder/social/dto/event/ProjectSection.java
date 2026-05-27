package com.yousef.cvbuilder.social.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectSection {

    private String projectName;
    private String technicalStack;
    private String projectUrl;
    private String architecturalSummary;
}
