package com.yousef.cvbuilder.cvmanagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yousef.cvbuilder.cvmanagement.dto.CvSaveRequest;
import com.yousef.cvbuilder.cvmanagement.entity.CvDocument;
import com.yousef.cvbuilder.cvmanagement.entity.EducationSection;
import com.yousef.cvbuilder.cvmanagement.entity.ExperienceSection;
import com.yousef.cvbuilder.cvmanagement.entity.LanguageSection;
import com.yousef.cvbuilder.cvmanagement.entity.ProjectSection;
import com.yousef.cvbuilder.cvmanagement.event.CvPublishedEvent;
import com.yousef.cvbuilder.cvmanagement.repository.CvRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class CvManagementServiceTest {

    @Mock
    private CvRepository cvRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private CvManagementService cvManagementService;

    @Test
    void saveDraftPersistsRichCvGraphWithAttachedChildren() {
        CvSaveRequest request = new CvSaveRequest();
        request.setUserId("user-1");
        request.setFullName("Yousef Ahmad");
        request.setTitle("Backend Engineer");
        request.setSummary("Builds distributed systems.");
        request.setSkills(List.of("Java", "Kafka"));
        request.setExperience(List.of(ExperienceSection.builder().companyName("Acme").jobTitle("Engineer").build()));
        request.setEducation(List.of(EducationSection.builder().institution("University").degreeLevel("Bachelor").build()));
        request.setProjects(List.of(ProjectSection.builder().projectName("CV Builder").technicalStack("Spring").build()));
        request.setLanguages(List.of(LanguageSection.builder().languageName("English").fluencyTier("Professional").build()));

        when(cvRepository.save(org.mockito.ArgumentMatchers.any(CvDocument.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CvDocument saved = cvManagementService.saveDraft(request);

        assertThat(saved.isPublished()).isFalse();
        assertThat(saved.getExperience()).hasSize(1);
        assertThat(saved.getExperience().get(0).getCv()).isSameAs(saved);
        assertThat(saved.getEducation().get(0).getCv()).isSameAs(saved);
        assertThat(saved.getProjects().get(0).getCv()).isSameAs(saved);
        assertThat(saved.getLanguages().get(0).getCv()).isSameAs(saved);
    }

    @Test
    void publishCvPublishesFullKafkaPayload() {
        CvDocument cv = CvDocument.builder()
                .id("cv-1")
                .userId("user-1")
                .fullName("Yousef Ahmad")
                .title("Backend Engineer")
                .summary("Builds distributed systems.")
                .skills(List.of("Java"))
                .experience(List.of(ExperienceSection.builder().companyName("Acme").build()))
                .education(List.of(EducationSection.builder().institution("University").build()))
                .projects(List.of(ProjectSection.builder().projectName("CV Builder").build()))
                .languages(List.of(LanguageSection.builder().languageName("Arabic").build()))
                .published(false)
                .build();

        when(cvRepository.findById("cv-1")).thenReturn(Optional.of(cv));
        when(cvRepository.save(cv)).thenReturn(cv);

        cvManagementService.publishCv("cv-1");

        ArgumentCaptor<CvPublishedEvent> eventCaptor = ArgumentCaptor.forClass(CvPublishedEvent.class);
        verify(kafkaTemplate).send(eq("cv-published-topic"), eq("user-1"), eventCaptor.capture());
        CvPublishedEvent event = eventCaptor.getValue();
        assertThat(event.getCvId()).isEqualTo("cv-1");
        assertThat(event.getExperience()).hasSize(1);
        assertThat(event.getEducation()).hasSize(1);
        assertThat(event.getProjects()).hasSize(1);
        assertThat(event.getLanguages()).hasSize(1);
        assertThat(cv.isPublished()).isTrue();
    }
}
