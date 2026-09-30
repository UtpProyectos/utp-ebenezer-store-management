package pe.edu.utp.ebenezer.api.dto.user;

import jakarta.validation.constraints.NotNull;

public record UserStatusRequest(
        @NotNull Boolean active
) {
}
