package com.yousef.cvbuilder.cvmanagement.repository;

import com.yousef.cvbuilder.cvmanagement.entity.CvDocument;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CvRepository extends JpaRepository<CvDocument, String> {

    List<CvDocument> findByUserId(String userId); // Fixed original typo from 'VoxDocument' to 'CvDocument'
}
