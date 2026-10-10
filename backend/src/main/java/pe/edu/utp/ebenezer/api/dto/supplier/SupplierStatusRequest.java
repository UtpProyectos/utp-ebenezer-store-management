package pe.edu.utp.ebenezer.api.dto.supplier;

import jakarta.validation.constraints.NotNull;

public record SupplierStatusRequest(@NotNull Boolean active) {
}
