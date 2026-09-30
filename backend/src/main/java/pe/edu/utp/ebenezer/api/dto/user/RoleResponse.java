package pe.edu.utp.ebenezer.api.dto.user;

import pe.edu.utp.ebenezer.domain.enums.RoleName;

public record RoleResponse(
        Long id,
        RoleName name,
        String description
) {
}
