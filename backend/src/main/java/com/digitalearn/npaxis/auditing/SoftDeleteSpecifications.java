package com.digitalearn.npaxis.auditing;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class SoftDeleteSpecifications {

    public static <T extends BaseEntity> Specification<T> isNotDeleted() {
        return new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.or(
                        cb.isNull(root.get("deleted")),
                        cb.equal(root.get("deleted"), false)
                );
            }
        };
    }

    public static <T extends BaseEntity> Specification<T> isDeleted() {
        return new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.equal(root.get("deleted"), true);
            }
        };
    }

    public static <T extends BaseEntity> Specification<T> includeDeleted() {
        return new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.conjunction();
            }
        };
    }

    public static <T extends BaseEntity> Specification<T> deletedAfter(LocalDateTime date) {
        return new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.and(
                        cb.equal(root.get("deleted"), true),
                        cb.greaterThanOrEqualTo(root.get("deletedAt"), date)
                );
            }
        };
    }

    public static <T extends BaseEntity> Specification<T> deletedBefore(LocalDateTime date) {
        return new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.and(
                        cb.equal(root.get("deleted"), true),
                        cb.lessThanOrEqualTo(root.get("deletedAt"), date)
                );
            }
        };
    }

    public static <T extends BaseEntity> Specification<T> deletedBy(String user) {
        return new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.and(
                        cb.equal(root.get("deleted"), true),
                        cb.equal(root.get("deletedBy"), user)
                );
            }
        };
    }

    public static <T extends BaseEntity> Specification<T> createdAfterAndNotDeleted(LocalDateTime date) {
        return new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.and(
                        isNotDeleted().toPredicate((Root<BaseEntity>) root, query, cb),
                        cb.greaterThanOrEqualTo(root.get("createdAt"), date)
                );
            }
        };
    }
}
