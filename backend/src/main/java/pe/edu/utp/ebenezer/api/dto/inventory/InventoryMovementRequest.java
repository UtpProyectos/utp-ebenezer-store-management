package pe.edu.utp.ebenezer.api.dto.inventory;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import pe.edu.utp.ebenezer.domain.enums.InventoryMovementType;

// Manual withdrawals (WASTE, RETURN to supplier). quantity is in the product base unit; the service applies the sign.
// Without lotId the quantity is taken from the lots with stock using FEFO.
public record InventoryMovementRequest(
        @NotNull Long productId,
        Long lotId,
        @NotNull InventoryMovementType movementType,
        @NotNull @Positive @Digits(integer = 12, fraction = 3) BigDecimal quantity,
        @Size(max = 150) String reason,
        @Size(max = 500) String notes
) {
}
