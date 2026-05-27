package com.yousef.cvbuilder.social.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CvPublishedEvent {

    private String cvId;
    private String userId;
    private String title;
    private String fullName;
    private String summary;
    private List<String> skills;
    private List<ExperienceSection> experience;
    private List<EducationSection> education;
    private List<ProjectSection> projects;
    private List<LanguageSection> languages;
}
