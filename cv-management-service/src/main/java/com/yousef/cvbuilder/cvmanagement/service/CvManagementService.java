package com.yousef.cvbuilder.cvmanagement.service;

import com.yousef.cvbuilder.cvmanagement.dto.CvSaveRequest;
import com.yousef.cvbuilder.cvmanagement.entity.CvDocument;
import com.yousef.cvbuilder.cvmanagement.entity.EducationSection;
import com.yousef.cvbuilder.cvmanagement.entity.ExperienceSection;
import com.yousef.cvbuilder.cvmanagement.entity.LanguageSection;
import com.yousef.cvbuilder.cvmanagement.entity.ProjectSection;
import com.yousef.cvbuilder.cvmanagement.event.CvPublishedEvent;
import com.yousef.cvbuilder.cvmanagement.repository.CvRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CvManagementService {

    private final CvRepository cvRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public CvDocument saveDraft(CvSaveRequest request) {
        CvDocument cv = CvDocument.builder()
                .id(UUID.randomUUID().toString())
                .userId(request.getUserId())
                .title(request.getTitle())
                .fullName(request.getFullName())
                .summary(request.getSummary())
                .skills(request.getSkills())
                .experience(request.getExperience())
                .education(request.getEducation())
                .projects(request.getProjects())
                .languages(request.getLanguages())
                .published(false) // Initialized as draft
                .build();
        attachChildren(cv);
        return cvRepository.save(cv);
    }

    @Transactional
    public CvDocument publishCv(String cvId) {
        CvDocument cv = cvRepository.findById(cvId)
                .orElseThrow(() -> new RuntimeException("CV Document not found"));

        cv.setPublished(true);
        CvDocument updatedCv = cvRepository.save(cv);

        // Build and publish the event to Kafka to be captured by the Social Feed Service
        CvPublishedEvent event = CvPublishedEvent.builder()
                .cvId(updatedCv.getId())
                .userId(updatedCv.getUserId())
                .title(updatedCv.getTitle())
                .fullName(updatedCv.getFullName())
                .summary(updatedCv.getSummary())
                .skills(updatedCv.getSkills())
                .experience(updatedCv.getExperience())
                .education(updatedCv.getEducation())
                .projects(updatedCv.getProjects())
                .languages(updatedCv.getLanguages())
                .build();

        kafkaTemplate.send("cv-published-topic", updatedCv.getUserId(), event);

        return updatedCv;
    }

    private void attachChildren(CvDocument cv) {
        safeList(cv.getExperience()).forEach(item -> {
            item.setId(null);
            item.setCv(cv);
        });
        safeList(cv.getEducation()).forEach(item -> {
            item.setId(null);
            item.setCv(cv);
        });
        safeList(cv.getProjects()).forEach(item -> {
            item.setId(null);
            item.setCv(cv);
        });
        safeList(cv.getLanguages()).forEach(item -> {
            item.setId(null);
            item.setCv(cv);
        });
    }

    private <T> List<T> safeList(List<T> items) {
        return items == null ? List.of() : items;
    }
}
