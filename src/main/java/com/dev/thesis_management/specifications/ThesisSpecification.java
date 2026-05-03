package com.dev.thesis_management.specifications;

import com.dev.thesis_management.thesis.entity.Thesis;
import com.dev.thesis_management.thesis.dto.thesis.ThesisSearchForm;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ThesisSpecification {

    public static Specification<Thesis> search(ThesisSearchForm form, UUID semesterId){

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if(form == null){
                return cb.conjunction();
            }

            Join<Object, Object> student = root.join("student", JoinType.LEFT);
            Join<Object, Object> program = student.join("program", JoinType.LEFT);
            Join<Object, Object> department = program.join("department", JoinType.LEFT);
            Join<Object, Object> faculty = program.join("faculty", JoinType.LEFT);
            Join<Object, Object> college = program.join("college", JoinType.LEFT);
            Join<Object, Object> departmentFaculty = department.join("faculty", JoinType.LEFT);

            // search by thesis title
            if(form.name() != null && !form.name().isBlank()){
                String keyword = "%" + form.name().toLowerCase() + "%";

                predicates.add(
                        cb.or(
                                cb.like(cb.lower(root.get("title")), keyword),
                                cb.like(cb.lower(root.get("titleEn")), keyword)
                        )
                );
            }

            // filter by student
            if(form.studentId() != null){
                predicates.add(
                        cb.equal(student.get("id"), form.studentId())
                );
            }

            // filter by mentor
            if(form.mentorId() != null){
                Join<Object, Object> topic = root.join("topic");
                Join<Object, Object> group = topic.join("group");
                Join<Object, Object> mentor = group.join("mentor");

                predicates.add(
                        cb.equal(mentor.get("id"), form.mentorId())
                );
            }

            if (semesterId != null) {
                Join<Object, Object> topic = root.join("topic");
                Join<Object, Object> group = topic.join("group");
                Join<Object, Object> semester = group.join("semester");

                predicates.add(cb.equal(semester.get("id"), semesterId));
            }

            // filter by manager academic scope fields in form
            if (form.programId() != null) {
                predicates.add(cb.equal(program.get("id"), form.programId()));
            }

            if (form.departmentId() != null) {
                predicates.add(cb.equal(department.get("id"), form.departmentId()));
            }

            if (form.facultyId() != null) {
                predicates.add(
                        cb.or(
                                cb.equal(faculty.get("id"), form.facultyId()),
                                cb.equal(departmentFaculty.get("id"), form.facultyId())
                        )
                );
            }

            if (form.collegeId() != null) {
                predicates.add(
                        cb.or(
                                cb.equal(college.get("id"), form.collegeId()),
                                cb.equal(faculty.get("college").get("id"), form.collegeId()),
                                cb.equal(departmentFaculty.get("college").get("id"), form.collegeId())
                        )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }


    public static Specification<Thesis> search(ThesisSearchForm form){

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if(form == null){
                return cb.conjunction();
            }

            // search by thesis title
            if(form.name() != null && !form.name().isBlank()){
                String keyword = "%" + form.name().toLowerCase() + "%";

                predicates.add(
                        cb.or(
                                cb.like(cb.lower(root.get("title")), keyword),
                                cb.like(cb.lower(root.get("titleEn")), keyword)
                        )
                );
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Thesis> search(ThesisSearchForm form, Thesis.AccessLevel accessLevel) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (form == null) {
                return cb.conjunction();
            }

            if (form.name() != null && !form.name().isBlank()) {
                String keyword = "%" + form.name().toLowerCase() + "%";

                predicates.add(
                        cb.or(
                                cb.like(cb.lower(root.get("title")), keyword),
                                cb.like(cb.lower(root.get("titleEn")), keyword)
                        )
                );
            }

            if (accessLevel != null) {
                predicates.add(
                        cb.equal(root.get("accessLevel"), accessLevel)
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Thesis> searchForManager(ThesisSearchForm form, UUID organizationId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<Object, Object> student = root.join("student", JoinType.LEFT);
            Join<Object, Object> program = student.join("program", JoinType.LEFT);
            Join<Object, Object> department = program.join("department", JoinType.LEFT);
            Join<Object, Object> faculty = program.join("faculty", JoinType.LEFT);

            predicates.add(cb.equal(student.get("organization").get("id"), organizationId));

            if (form == null) return cb.and(predicates.toArray(new Predicate[0]));

            if (form.name() != null && !form.name().isBlank()) {
                String keyword = "%" + form.name().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), keyword),
                        cb.like(cb.lower(root.get("titleEn")), keyword)
                ));
            }

            if(form.status() != null){
                predicates.add(
                        cb.equal(root.get("status"), form.status())
                );
            }

            if(form.accessLevel() != null){
                predicates.add(
                        cb.equal(root.get("accessLevel"), form.accessLevel())
                );
            }

            if (form.semesterId() != null) {
                predicates.add(cb.equal(root.join("topic").join("group").get("semester").get("id"), form.semesterId()));
            }

            // FILTER COLLEGE
            if (form.collegeId() != null) {
                // Kiểm tra College thông qua Faculty của Program sinh viên
                predicates.add(cb.equal(faculty.get("college").get("id"), form.collegeId()));
            }

            // FILTER FACULTY
            if (form.facultyId() != null) {
                predicates.add(cb.equal(faculty.get("id"), form.facultyId()));
            }

            // FILTER DEPARTMENT
            if (form.departmentId() != null) {
                predicates.add(cb.equal(department.get("id"), form.departmentId()));
            }

            // FILTER PROGRAM
            if (form.programId() != null) {
                predicates.add(cb.equal(program.get("id"), form.programId()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}