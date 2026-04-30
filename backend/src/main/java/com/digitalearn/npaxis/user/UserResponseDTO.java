package com.digitalearn.npaxis.user;

public record UserResponseDTO(

        Long userId,
        String displayName,
        String email,
        String role,
        boolean accountEnabled,
        String photoUrl
) {
}
