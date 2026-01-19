package com.leetcoder.infrastructure.repository;

import com.leetcoder.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    // Basic CRUD needed
    boolean existsByEmail(String email);

    boolean existsByLeetcodeUsername(String username);
}
