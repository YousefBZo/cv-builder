package com.yousef.cvbuilder.identity.repository;

import com.yousef.cvbuilder.identity.entity.UserAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserAccount, String> {

    Optional<UserAccount> findByEmail(String email);
}
