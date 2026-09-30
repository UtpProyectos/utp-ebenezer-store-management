package pe.edu.utp.ebenezer.api.dto.shoppinglist;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ShoppingListDetailRequest(
        @NotNull Long productId,
        Long supplierId,
        @NotNull Long unitOfMeasureId,
        @NotNull @Positive @Digits(integer = 12, fraction = 3) BigDecimal suggestedQuantity,
        @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal estimatedCost
) {
}
