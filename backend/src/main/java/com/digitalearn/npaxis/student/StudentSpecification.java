package com.digitalearn.npaxis.student;

import com.digitalearn.npaxis.student.Student;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import java.util.List;

/**
 * Specification builder for Student entity.
 */
public class StudentSpecification {

    private StudentSpecification() {
        /* utility class */
    }

    public static Specification<Student> isActive() {
        return (root, query, cb) ->
                cb.isTrue(root.get("active")); // if exists
    }

    public static Specification<Student> isNotDeleted() {
        return (root, query, cb) ->
                cb.isFalse(root.get("deleted"));
    }

    public static Specification<Student> hasUniversity(String university) {
        return (root, query, cb) ->
                university == null ? null :
                        cb.like(cb.lower(root.get("university")),
                                "%" + university.toLowerCase() + "%");
    }

    public static Specification<Student> hasProgram(String program) {
        return (root, query, cb) ->
                program == null ? null :
                        cb.like(cb.lower(root.get("program")),
                                "%" + program.toLowerCase() + "%");
    }

    public static Specification<Student> hasGraduationYear(String year) {
        return (root, query, cb) ->
                year == null ? null :
                        cb.equal(root.get("graduationYear"), year);
    }

    public static Specification<Student> hasPhone(String phone) {
        return (root, query, cb) ->
                phone == null ? null :
                        cb.like(root.get("phone"), "%" + phone + "%");
    }

    /**
     * Filter students who saved specific preceptors.
     */
    public static Specification<Student> hasSavedPreceptors(List<Long> preceptorIds) {
        return (root, query, cb) -> {

            if (preceptorIds == null || preceptorIds.isEmpty()) return null;

            query.distinct(true);

            Join<Object, Object> join = root.join("savedPreceptors");

            return join.get("userId").in(preceptorIds);
        };
    }
}