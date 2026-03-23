package com.dev.thesis_management.thesis.repository;

import com.dev.thesis_management.thesis.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MeetingRepository extends JpaRepository<Meeting, UUID> {
}
