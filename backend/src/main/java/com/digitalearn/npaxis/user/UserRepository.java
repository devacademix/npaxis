package com.digitalearn.npaxis.user;

import com.digitalearn.npaxis.auditing.BaseRepository;
import com.digitalearn.npaxis.role.Role;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing User entities.
 * This interface extends JpaRepository to provide CRUD operations and custom query methods.
 */
@Repository(value = "UserRepository")
public interface UserRepository extends BaseRepository<User, Long> {

    @Modifying
    @Query(value = "DELETE FROM users WHERE user_id = ?1", nativeQuery = true)
    @Transactional
    void hardDeleteById(Long id);


    /**
     * Finds a User by their email.
     *
     * @param email The email of the user to search for.
     * @return An Optional containing the User if found, or empty if not found.
     */
    boolean existsByEmail(String email);

    /**
     * Finds a User by their email.
     *
     * @param email The email of the user to search for.
     * @return An Optional containing the User if found, or empty if not found.
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds a User by their username.
     *
     * @param username The username of the user to search for.
     * @return An Optional containing the User if found, or empty if not found.
     */
    Optional<User> findByUsername(String username);

    List<User> findAllByRole(Role role);

    @Query("SELECT COUNT(u) FROM User u")
    Long countTotalUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startOfMonth")
    Long countNewUsersThisMonth(LocalDateTime startOfMonth);
}
