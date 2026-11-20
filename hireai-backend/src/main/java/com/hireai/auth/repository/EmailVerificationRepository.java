package com.hireai.auth.repository;

import com.hireai.auth.entity.EmailVerification;
import com.hireai.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, UUID> {

    @Query("SELECT ev FROM EmailVerification ev WHERE ev.user = :user AND ev.consumedAt IS NULL ORDER BY ev.createdAt DESC LIMIT 1")
    Optional<EmailVerification> findLatestActiveByUser(@Param("user") User user);
}
