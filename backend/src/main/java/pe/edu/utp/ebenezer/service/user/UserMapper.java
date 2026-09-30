package pe.edu.utp.ebenezer.service.user;

import pe.edu.utp.ebenezer.api.dto.user.RoleResponse;
import pe.edu.utp.ebenezer.api.dto.user.UserResponse;
import pe.edu.utp.ebenezer.domain.entity.Role;
import pe.edu.utp.ebenezer.domain.entity.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().getName(),
                user.getActive(),
                user.getLastLoginAt(),
                user.getCreatedAt());
    }

    public static RoleResponse toResponse(Role role) {
        return new RoleResponse(role.getId(), role.getName(), role.getDescription());
    }
}
