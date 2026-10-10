package pe.edu.utp.ebenezer.api.dto.purchase;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// subtotal is the total cost of the line; the service derives the unit cost.
// Lot data (lotCode, expirationDate) is used to create the Lot for this detail.
// salePrice is optional: when present it updates the product sale price.
public record PurchaseDetailRequest(
        @NotNull Long productId,
        @NotNull Long unitOfMeasureId,
        @NotNull @Positive @Digits(integer = 12, fraction = 3) BigDecimal quantity,
        @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal subtotal,
        @Positive @Digits(integer = 10, fraction = 2) BigDecimal salePrice,
        @Size(max = 100) String lotCode,
        LocalDate expirationDate
) {
}
