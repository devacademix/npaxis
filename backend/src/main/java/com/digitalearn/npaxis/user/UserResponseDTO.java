package com.digitalearn.npaxis.user;

public record UserResponseDTO(

        Long userId,
        String displayName,
        String email,
<<<<<<< HEAD
        String role
=======
        String role,
        boolean accountEnabled,
        String photoUrl
>>>>>>> 0e3228387c826f857fc2a85b5c95a9762b545eef
) {
}
