package pe.edu.utp.ebenezer.api.dto.sale;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record SaleDetailRequest(
        @NotNull Long productId,
        @NotNull Long unitOfMeasureId,
        @NotNull @Positive @Digits(integer = 12, fraction = 3) BigDecimal quantity,
        @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal discount
) {
}
