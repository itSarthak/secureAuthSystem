package com.pipeline.backend.repository;

import com.pipeline.backend.entity.authCode.AuthCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthRepository extends JpaRepository<AuthCode, UUID> {

    Optional<AuthCode> findTopByUserIdOrderByExpiresAtDesc(UUID userId);
}
