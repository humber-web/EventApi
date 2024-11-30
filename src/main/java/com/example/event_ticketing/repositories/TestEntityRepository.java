package com.example.event_ticketing.repositories;

import com.example.event_ticketing.models.TestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestEntityRepository extends JpaRepository<TestEntity, Long> {
}
