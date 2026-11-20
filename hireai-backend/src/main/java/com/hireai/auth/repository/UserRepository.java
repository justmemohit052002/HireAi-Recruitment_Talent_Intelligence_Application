package com.hireai.auth.repository;

import com.hireai.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByWorkEmailIgnoreCase(String workEmail);

    boolean existsByWorkEmailIgnoreCase(String workEmail);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles LEFT JOIN FETCH u.organization WHERE LOWER(u.workEmail) = LOWER(:email)")
    Optional<User> findByWorkEmailWithDetails(@Param("email") String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles LEFT JOIN FETCH u.organization WHERE u.id = :id")
    Optional<User> findByIdWithDetails(@Param("id") UUID id);
}
