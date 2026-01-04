package com.pipeline.backend.repository;

import com.pipeline.backend.entity.authCode.AuthCode;
import com.pipeline.backend.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuthRepository extends JpaRepository<AuthCode, UUID> {
}
