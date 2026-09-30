package pe.edu.utp.ebenezer.api.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import pe.edu.utp.ebenezer.domain.enums.RoleName;

public record UserCreateRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(min = 3, max = 80) String username,
        @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotNull RoleName role
) {
}
