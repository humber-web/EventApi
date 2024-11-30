package com.example.event_ticketing.repositories;

import com.example.event_ticketing.models.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional <User> findByEmail(String email);
    // Custom query if needed (e.g., findByEmail)
}
