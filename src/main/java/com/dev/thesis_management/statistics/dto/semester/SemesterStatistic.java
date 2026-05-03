package com.dev.thesis_management.statistics.dto.semester;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SemesterStatistic {
	SemesterDetail semester;
	SemesterMetrics metrics;
	List<ThesisInsight> theses;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@FieldDefaults(level = AccessLevel.PRIVATE)
	public static class SemesterDetail {
		UUID id;
		String name;
		LocalDate startDate;
		LocalDate endDate;
		String status;
		UUID organizationId;
		String organizationName;
		UUID academicYearId;
		String academicYearName;
		LocalDateTime createdAt;
		LocalDateTime updatedAt;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@FieldDefaults(level = AccessLevel.PRIVATE)
	public static class SemesterMetrics {
		long totalStudents;
		long totalMentors;
		long totalGroups;
		long totalTopics;
		long totalTheses;
		long proposalTheses;
		long inProgressTheses;
		long submittedTheses;
		long gradedTheses;
		long totalDefenses;
		long scheduledDefenses;
		long completedDefenses;
		long cancelledDefenses;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@FieldDefaults(level = AccessLevel.PRIVATE)
	public static class ThesisInsight {
		UUID thesisId;
		String title;
		String titleEn;
		String description;
		String descriptionEn;
		String status;
		String accessLevel;
		Integer progressPercent;
		LocalDateTime createdAt;
		TopicBrief topic;
		GroupBrief group;
		StudentBrief student;
		MentorBrief mentor;
		DefenseBrief defense;
		Double averageScore;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@FieldDefaults(level = AccessLevel.PRIVATE)
	public static class TopicBrief {
		UUID id;
		String title;
		String description;
		Integer maxStudents;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@FieldDefaults(level = AccessLevel.PRIVATE)
	public static class GroupBrief {
		UUID id;
		String name;
		String description;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@FieldDefaults(level = AccessLevel.PRIVATE)
	public static class StudentBrief {
		UUID id;
		String code;
		String fullName;
		String email;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@FieldDefaults(level = AccessLevel.PRIVATE)
	public static class MentorBrief {
		UUID id;
		String code;
		String fullName;
		String email;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@FieldDefaults(level = AccessLevel.PRIVATE)
	public static class DefenseBrief {
		UUID id;
		String status;
		LocalDateTime defenseTime;
		String location;
		CouncilBrief council;
		List<ScoreBrief> scores;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@FieldDefaults(level = AccessLevel.PRIVATE)
	public static class CouncilBrief {
		UUID id;
		String code;
		String name;
		List<CouncilMemberBrief> members;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@FieldDefaults(level = AccessLevel.PRIVATE)
	public static class CouncilMemberBrief {
		UUID id;
		UUID lecturerId;
		String lecturerCode;
		String lecturerName;
		String roleCode;
		String roleName;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@FieldDefaults(level = AccessLevel.PRIVATE)
	public static class ScoreBrief {
		UUID id;
		UUID councilMemberId;
		UUID lecturerId;
		String lecturerCode;
		String lecturerName;
		String roleCode;
		String roleName;
		Double score;
		String comment;
		LocalDateTime createdAt;
	}
}
