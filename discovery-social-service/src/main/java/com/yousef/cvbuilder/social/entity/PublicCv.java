package com.yousef.cvbuilder.social.entity;

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
@Table(name = "public_cvs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicCv {

    @Id
    private String id; // Matches the original cvId root
    private String userId;
    private String title;
    private String fullName;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @ElementCollection
    @CollectionTable(name = "public_cv_skills", joinColumns = @JoinColumn(name = "public_cv_id"))
    @Column(name = "skill")
    @Fetch(FetchMode.SUBSELECT)
    private List<String> skills;

    @OneToMany(mappedBy = "publicCv", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private List<PublicExperienceSection> experience;

    @OneToMany(mappedBy = "publicCv", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private List<PublicEducationSection> education;

    @OneToMany(mappedBy = "publicCv", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private List<PublicProjectSection> projects;

    @OneToMany(mappedBy = "publicCv", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private List<PublicLanguageSection> languages;
}
