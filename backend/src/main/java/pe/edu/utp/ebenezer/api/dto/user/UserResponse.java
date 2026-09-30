package pe.edu.utp.ebenezer.api.dto.user;

import java.time.LocalDateTime;

import pe.edu.utp.ebenezer.domain.enums.RoleName;

public record UserResponse(
        Long id,
        String name,
        String username,
        String email,
        RoleName role,
        Boolean active,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt
) {
}
