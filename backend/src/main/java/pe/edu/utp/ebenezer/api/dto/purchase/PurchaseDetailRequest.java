package pe.edu.utp.ebenezer.api.dto.purchase;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

// Lot data (lotCode, expirationDate) is used to create the Lot for this detail.
public record PurchaseDetailRequest(
        @NotNull Long productId,
        @NotNull Long unitOfMeasureId,
        @NotNull @Positive @Digits(integer = 12, fraction = 3) BigDecimal quantity,
        @NotNull @PositiveOrZero @Digits(integer = 8, fraction = 4) BigDecimal unitCost,
        @Size(max = 100) String lotCode,
        LocalDate expirationDate
) {
}
