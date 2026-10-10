package pe.edu.utp.ebenezer.api.dto.category;

import jakarta.validation.constraints.NotNull;

public record CategoryStatusRequest(@NotNull Boolean active) {
}
