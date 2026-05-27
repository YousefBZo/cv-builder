package com.yousef.cvbuilder.social.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "public_cv_experience_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicExperienceSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "public_cv_id", nullable = false)
    private PublicCv publicCv;

    private String companyName;
    private String jobTitle;
    private String startDate;
    private String endDate;

    @Column(columnDefinition = "TEXT")
    private String responsibilities;
}
