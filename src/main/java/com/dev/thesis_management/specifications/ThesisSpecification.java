package com.dev.thesis_management.specifications;

import com.dev.thesis_management.thesis.entity.Semester;
import com.dev.thesis_management.thesis.entity.Thesis;
import com.dev.thesis_management.thesis.dto.thesis.ThesisSearchForm;
import jakarta.persistence.criteria.Join;
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
                Join<Object, Object> student = root.join("student");
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

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}