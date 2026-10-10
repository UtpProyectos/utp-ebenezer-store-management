package pe.edu.utp.ebenezer.api.dto.product;

import jakarta.validation.constraints.NotNull;

public record ProductStatusRequest(@NotNull Boolean active) {
}
