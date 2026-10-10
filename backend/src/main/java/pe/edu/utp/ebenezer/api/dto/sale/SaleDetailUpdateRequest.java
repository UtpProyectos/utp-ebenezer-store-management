package pe.edu.utp.ebenezer.api.dto.sale;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

// New quantity of an existing line, in the unit of that line. Zero removes the product from the sale.
public record SaleDetailUpdateRequest(
        @NotNull Long saleDetailId,
        @NotNull @PositiveOrZero @Digits(integer = 12, fraction = 3) BigDecimal quantity
) {
}
