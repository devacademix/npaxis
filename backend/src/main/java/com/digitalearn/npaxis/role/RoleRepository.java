package com.digitalearn.npaxis.role;

import com.digitalearn.npaxis.auditing.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing Role entities.
 * This interface extends BaseRepository to provide CRUD operations and custom query methods.
 */
@Repository
public interface RoleRepository extends BaseRepository<Role, Long> {

    /**
     * Finds a Role by its name.
     *
     * @param roleName The name of the role to search for.
     * @return An Optional containing the Role if found, or empty if not found.
     */
    Optional<Role> findByRoleName(RoleName roleName);

    /**
     * Checks if a Role with the specified name exists.
     *
     * @param roleName The name of the role to check for.
     * @return true if the role exists, false otherwise.
     */
    boolean existsByRoleName(RoleName roleName);

}
