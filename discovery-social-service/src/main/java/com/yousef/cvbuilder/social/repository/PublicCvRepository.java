package com.yousef.cvbuilder.social.repository;

import com.yousef.cvbuilder.social.entity.PublicCv;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicCvRepository extends JpaRepository<PublicCv, String> {
}
