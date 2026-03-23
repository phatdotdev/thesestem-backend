package com.dev.thesis_management.specifications;

import com.dev.thesis_management.thesis.dto.defense.DefenseSearchForm;
import com.dev.thesis_management.thesis.entity.ThesisDefense;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ThesisDefenseSpecification {

    public static Specification<ThesisDefense> search(DefenseSearchForm form, UUID semesterId){

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if(form == null){
                return cb.conjunction();
            }

            // join thesis -> topic -> group -> semester
            Join<Object, Object> thesis = root.join("thesis");
            Join<Object, Object> topic = thesis.join("topic");
            Join<Object, Object> group = topic.join("group");

            // filter semester
            if(semesterId != null){

                Join<Object, Object> semester = group.join("semester");

                predicates.add(
                        cb.equal(semester.get("id"), semesterId)
                );
            }


            // filter council
            if(form.councilId() != null){

                Join<Object, Object> council = root.join("council");

                predicates.add(
                        cb.equal(council.get("id"), form.councilId())
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}