package com.dev.thesis_management.statistics.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SystemStatistics {
    long totalUsers;
    long totalManagers;
    long totalStudents;
    long totalLecturers;
    long totalTheses;
    long totalPublishedTheses;
    long totalInternalTheses;
    long totalPrivateTheses;
}
