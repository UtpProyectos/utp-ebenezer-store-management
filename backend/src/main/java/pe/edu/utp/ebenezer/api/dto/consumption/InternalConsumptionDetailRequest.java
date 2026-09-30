package pe.edu.utp.ebenezer.api.dto.consumption;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record InternalConsumptionDetailRequest(
        @NotNull Long productId,
        @NotNull Long unitOfMeasureId,
        @NotNull @Positive @Digits(integer = 12, fraction = 3) BigDecimal quantity
) {
}
