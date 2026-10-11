package pe.edu.utp.ebenezer.api.dto.shoppinglist;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;

// Both fields are optional: only the ones sent are changed. Quantity is in the unit of the detail.
public record ShoppingListDetailUpdateRequest(
        @Positive @Digits(integer = 12, fraction = 3) BigDecimal suggestedQuantity,
        Boolean purchased
) {
}
