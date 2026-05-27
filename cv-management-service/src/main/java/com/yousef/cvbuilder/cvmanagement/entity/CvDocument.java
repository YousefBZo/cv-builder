package com.yousef.cvbuilder.cvmanagement.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "cv_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CvDocument {

    @Id
    private String id;

    @Column(nullable = false)
    private String userId; // Logical relationship pointing to Identity Service

    private String title;
    private String fullName;
    private String summary;

    @ElementCollection
    @CollectionTable(name = "cv_skills", joinColumns = @JoinColumn(name = "cv_id"))
    @Column(name = "skill")
    @Fetch(FetchMode.SUBSELECT)
    private List<String> skills;

    @OneToMany(mappedBy = "cv", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private List<ExperienceSection> experience;

    @OneToMany(mappedBy = "cv", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private List<EducationSection> education;

    @OneToMany(mappedBy = "cv", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private List<ProjectSection> projects;

    @OneToMany(mappedBy = "cv", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private List<LanguageSection> languages;

    @Column(nullable = false)
    private boolean published; // CV state flag (false = Draft, true = Publicly Published)
}
