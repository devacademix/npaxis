package com.digitalearn.npaxis.student;

import com.digitalearn.npaxis.auditing.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing Student entities.
 */
@Repository(value = "StudentRepository")
public interface StudentRepository extends BaseRepository<Student, Long> {
}
