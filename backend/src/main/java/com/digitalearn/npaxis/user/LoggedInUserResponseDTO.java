package com.digitalearn.npaxis.user;

import lombok.Builder;

@Builder
public record LoggedInUserResponseDTO(
        Long userId,
        String username,
        String name,
        String email,
        String photoUrl
) {
}
