package pe.edu.utp.ebenezer.api.dto.promotion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record PromotionRequest(
        @NotNull Long productId,
        @NotNull Long unitOfMeasureId,
        @Size(max = 150) String name,
        @NotNull @Positive @Digits(integer = 12, fraction = 3) BigDecimal promotionQuantity,
        @NotNull @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal promotionalPrice,
        Boolean repeatable,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
