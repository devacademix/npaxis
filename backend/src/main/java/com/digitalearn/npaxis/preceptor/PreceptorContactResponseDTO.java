package com.digitalearn.npaxis.preceptor;

import lombok.Builder;

@Builder
public record PreceptorContactResponseDTO(
        String phone,
        String email
) {
}
