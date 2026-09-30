package pe.edu.utp.ebenezer.api.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import pe.edu.utp.ebenezer.domain.enums.RoleName;

public record UserUpdateRequest(
        @NotBlank @Size(max = 120) String name,
        @Email @Size(max = 150) String email,
        @NotNull RoleName role
) {
}
