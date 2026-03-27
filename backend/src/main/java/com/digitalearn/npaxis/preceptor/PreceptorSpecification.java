package com.digitalearn.npaxis.preceptor;

import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class PreceptorSpecification {
    private PreceptorSpecification() {
        /* This utility class should not be instantiated */
    }


    public static Specification<Preceptor> isActive() {
        // BaseEntity does not persist an `active` column; active records are `deleted = false`.
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static <T> Specification<T> isNotDeleted() {
        return (root, query, cb) ->
                cb.isFalse(root.get("deleted"));
    }

    public static Specification<Preceptor> hasSpecialty(String specialty) {
        return (root, query, cb) ->
                specialty == null ? null :
                        cb.like(cb.lower(root.get("specialty")),
                                "%" + specialty.toLowerCase() + "%");
    }

    public static Specification<Preceptor> hasLocation(String location) {
        return (root, query, cb) ->
                location == null ? null :
                        cb.like(cb.lower(root.get("location")),
                                "%" + location.toLowerCase() + "%");
    }

    public static Specification<Preceptor> hasAvailableDays(List<DayOfWeekEnum> days) {
        return (root, query, cb) -> {

            if (days == null || days.isEmpty()) return null;

            Join<Object, Object> join = root.join("availableDays");

            return join.in(days);
        };
    }

    public static Specification<Preceptor> honorariumBetween(Integer min, Integer max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;

            if (min != null && max != null) {
                return cb.between(root.get("honorarium"), min, max);
            }

            if (min != null) {
                return cb.greaterThanOrEqualTo(root.get("honorarium"), min);
            }

            return cb.lessThanOrEqualTo(root.get("honorarium"), max);
        };
    }
}
