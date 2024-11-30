package com.example.event_ticketing.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.event_ticketing.repositories.TestEntityRepository;
import com.example.event_ticketing.models.TestEntity;

@Service
public class TestEntityService {
    private final TestEntityRepository repository;

    public TestEntityService(TestEntityRepository repository) {
        this.repository = repository;
    }

    public List<TestEntity> getAll() {
        return repository.findAll();
    }

    public TestEntity create(String name) {
        TestEntity entity = new TestEntity();
        entity.setName(name);
        return repository.save(entity);
    }
}
