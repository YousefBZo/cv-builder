package com.yousef.cvbuilder.cvmanagement.event;

import com.yousef.cvbuilder.cvmanagement.entity.EducationSection;
import com.yousef.cvbuilder.cvmanagement.entity.ExperienceSection;
import com.yousef.cvbuilder.cvmanagement.entity.LanguageSection;
import com.yousef.cvbuilder.cvmanagement.entity.ProjectSection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
