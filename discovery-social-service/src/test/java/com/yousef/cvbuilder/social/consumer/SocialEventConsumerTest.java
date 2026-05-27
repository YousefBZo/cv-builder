package com.yousef.cvbuilder.social.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.yousef.cvbuilder.social.dto.event.CvPublishedEvent;
import com.yousef.cvbuilder.social.dto.event.EducationSection;
import com.yousef.cvbuilder.social.dto.event.ExperienceSection;
import com.yousef.cvbuilder.social.dto.event.LanguageSection;
import com.yousef.cvbuilder.social.dto.event.ProjectSection;
import com.yousef.cvbuilder.social.dto.event.UserRegisteredEvent;
import com.yousef.cvbuilder.social.entity.PublicCv;
import com.yousef.cvbuilder.social.entity.UserProfile;
import com.yousef.cvbuilder.social.repository.PublicCvRepository;
import com.yousef.cvbuilder.social.repository.UserProfileRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SocialEventConsumerTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private PublicCvRepository publicCvRepository;

    @InjectMocks
    private SocialEventConsumer socialEventConsumer;

    @Test
    void userRegistrationEventCreatesLocalProfileProjection() {
        UserRegisteredEvent event = new UserRegisteredEvent("user-1", "user@example.com", "User One");

        socialEventConsumer.consumeUserRegistration(event);

        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo("user-1");
        assertThat(captor.getValue().getEmail()).isEqualTo("user@example.com");
    }

    @Test
    void cvPublishedEventCreatesPublicCvGraphProjection() {
        CvPublishedEvent event = new CvPublishedEvent(
                "cv-1",
                "user-1",
                "Backend Engineer",
                "Yousef Ahmad",
                "Builds distributed systems.",
                List.of("Java", "Kafka"),
                List.of(new ExperienceSection("Acme", "Engineer", "2024", "2026", "Built APIs")),
                List.of(new EducationSection("University", "Software Engineering", "Bachelor", "2026")),
                List.of(new ProjectSection("CV Builder", "Spring, Kafka", "https://example.com", "Microservices")),
                List.of(new LanguageSection("English", "Professional")));

        socialEventConsumer.consumeCvPublished(event);

        ArgumentCaptor<PublicCv> captor = ArgumentCaptor.forClass(PublicCv.class);
        verify(publicCvRepository).save(captor.capture());
        PublicCv saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo("cv-1");
        assertThat(saved.getSkills()).containsExactly("Java", "Kafka");
        assertThat(saved.getExperience()).hasSize(1);
        assertThat(saved.getExperience().get(0).getPublicCv()).isSameAs(saved);
        assertThat(saved.getEducation().get(0).getPublicCv()).isSameAs(saved);
        assertThat(saved.getProjects().get(0).getPublicCv()).isSameAs(saved);
        assertThat(saved.getLanguages().get(0).getPublicCv()).isSameAs(saved);
    }
}
