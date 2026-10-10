package pe.edu.utp.ebenezer.api.dto.product;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductRequest(
        @NotNull Long categoryId,
        @NotNull Long baseUnitId,
        @NotBlank @Size(max = 150) String name,
        @Size(max = 300) String description,
        @Size(max = 100) String barcode,
        // Optional: the sale price is normally set on each purchase entry (Ingreso de mercadería).
        @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal salePrice,
        @PositiveOrZero @Digits(integer = 12, fraction = 3) BigDecimal minStock
) {
}
