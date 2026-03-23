package com.dev.thesis_management.thesis.repository;

import com.dev.thesis_management.thesis.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TopicRepository extends JpaRepository<Topic, UUID> {
}
