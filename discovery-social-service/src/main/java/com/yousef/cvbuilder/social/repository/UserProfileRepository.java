package com.yousef.cvbuilder.social.repository;

import com.yousef.cvbuilder.social.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, String> {
}
