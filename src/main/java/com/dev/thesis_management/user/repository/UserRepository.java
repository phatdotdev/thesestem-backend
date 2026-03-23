package com.dev.thesis_management.user.repository;

import com.dev.thesis_management.user.entity.User;
import com.dev.thesis_management.user.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    Optional<User> findByUsernameAndRoleIn(String username, List<UserRole> roles);
}
