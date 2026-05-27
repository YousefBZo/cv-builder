package com.yousef.cvbuilder.social.consumer;

import com.yousef.cvbuilder.social.dto.event.CvPublishedEvent;
import com.yousef.cvbuilder.social.dto.event.UserRegisteredEvent;
import com.yousef.cvbuilder.social.entity.PublicCv;
import com.yousef.cvbuilder.social.entity.PublicEducationSection;
import com.yousef.cvbuilder.social.entity.PublicExperienceSection;
import com.yousef.cvbuilder.social.entity.PublicLanguageSection;
import com.yousef.cvbuilder.social.entity.PublicProjectSection;
import com.yousef.cvbuilder.social.entity.UserProfile;
import com.yousef.cvbuilder.social.repository.PublicCvRepository;
import com.yousef.cvbuilder.social.repository.UserProfileRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SocialEventConsumer {

    private final UserProfileRepository userProfileRepository;
    private final PublicCvRepository publicCvRepository;

    @KafkaListener(topics = "user-registration-topic", groupId = "social-feed-group")
    public void consumeUserRegistration(UserRegisteredEvent event) {
        log.info("Received User Registration Event for user: {}", event.getUserId());
        UserProfile profile = UserProfile.builder()
                .id(event.getUserId())
                .email(event.getEmail())
                .fullName(event.getFullName())
                .build();
        userProfileRepository.save(profile);
    }

    @KafkaListener(topics = "cv-published-topic", groupId = "social-feed-group")
    public void consumeCvPublished(CvPublishedEvent event) {
        log.info("Received CV Published Event for CV ID: {}", event.getCvId());
        PublicCv publicCv = PublicCv.builder()
                .id(event.getCvId())
                .userId(event.getUserId())
                .title(event.getTitle())
                .fullName(event.getFullName())
                .summary(event.getSummary())
                .skills(event.getSkills())
                .experience(mapExperience(event.getExperience()))
                .education(mapEducation(event.getEducation()))
                .projects(mapProjects(event.getProjects()))
                .languages(mapLanguages(event.getLanguages()))
                .build();
        attachChildren(publicCv);
        publicCvRepository.save(publicCv);
    }

    private void attachChildren(PublicCv publicCv) {
        safeList(publicCv.getExperience()).forEach(item -> {
            item.setId(null);
            item.setPublicCv(publicCv);
        });
        safeList(publicCv.getEducation()).forEach(item -> {
            item.setId(null);
            item.setPublicCv(publicCv);
        });
        safeList(publicCv.getProjects()).forEach(item -> {
            item.setId(null);
            item.setPublicCv(publicCv);
        });
        safeList(publicCv.getLanguages()).forEach(item -> {
            item.setId(null);
            item.setPublicCv(publicCv);
        });
    }

    private List<PublicExperienceSection> mapExperience(
            List<com.yousef.cvbuilder.social.dto.event.ExperienceSection> experience) {
        return safeList(experience).stream()
                .map(item -> PublicExperienceSection.builder()
                        .companyName(item.getCompanyName())
                        .jobTitle(item.getJobTitle())
                        .startDate(item.getStartDate())
                        .endDate(item.getEndDate())
                        .responsibilities(item.getResponsibilities())
                        .build())
                .toList();
    }

    private List<PublicEducationSection> mapEducation(
            List<com.yousef.cvbuilder.social.dto.event.EducationSection> education) {
        return safeList(education).stream()
                .map(item -> PublicEducationSection.builder()
                        .institution(item.getInstitution())
                        .fieldOfStudy(item.getFieldOfStudy())
                        .degreeLevel(item.getDegreeLevel())
                        .graduationYear(item.getGraduationYear())
                        .build())
                .toList();
    }

    private List<PublicProjectSection> mapProjects(
            List<com.yousef.cvbuilder.social.dto.event.ProjectSection> projects) {
        return safeList(projects).stream()
                .map(item -> PublicProjectSection.builder()
                        .projectName(item.getProjectName())
                        .technicalStack(item.getTechnicalStack())
                        .projectUrl(item.getProjectUrl())
                        .architecturalSummary(item.getArchitecturalSummary())
                        .build())
                .toList();
    }

    private List<PublicLanguageSection> mapLanguages(
            List<com.yousef.cvbuilder.social.dto.event.LanguageSection> languages) {
        return safeList(languages).stream()
                .map(item -> PublicLanguageSection.builder()
                        .languageName(item.getLanguageName())
                        .fluencyTier(item.getFluencyTier())
                        .build())
                .toList();
    }

    private <T> List<T> safeList(List<T> items) {
        return items == null ? List.of() : items;
    }
}
