package com.digitalearn.npaxis.role;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum representing the different roles available in the system.
 * Each role is associated with a specific set of permissions and responsibilities.
 */
@Getter
@AllArgsConstructor
public enum RoleName {
    ROLE_STUDENT("STUDENT"),
    ROLE_PRECEPTOR("PRECEPTOR"),
    ROLE_ADMIN("ADMIN");

    private final String roleName;

    public String getRoleName() {
        return roleName;
    }
}