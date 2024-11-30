package com.example.event_ticketing.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.event_ticketing.services.TestEntityService;
import com.example.event_ticketing.models.TestEntity;

import java.util.List;

@RestController
@RequestMapping("/health")
public class HealthController {

    @Autowired
    private TestEntityService testEntityService;

    @GetMapping
    public String checkHealth() {
        return "API is running!";
    }

    @GetMapping("/db-test")
    public List<TestEntity> testDb() {
        testEntityService.create("Sample Data");
        return testEntityService.getAll();
    }
}