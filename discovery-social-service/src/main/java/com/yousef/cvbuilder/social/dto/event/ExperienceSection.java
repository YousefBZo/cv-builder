package com.yousef.cvbuilder.social.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExperienceSection {

    private String companyName;
    private String jobTitle;
    private String startDate;
    private String endDate;
    private String responsibilities;
}
