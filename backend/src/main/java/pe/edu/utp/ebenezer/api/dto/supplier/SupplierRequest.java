package pe.edu.utp.ebenezer.api.dto.supplier;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import pe.edu.utp.ebenezer.domain.enums.SupplierType;

public record SupplierRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 20) String documentNumber,
        @Size(max = 30) String phone,
        SupplierType type,
        @Size(max = 150) String contactName,
        @Size(max = 250) String address,
        @Size(max = 500) String notes,
        @Size(max = 8) List<@NotNull @Positive Long> productIds
) {
}
