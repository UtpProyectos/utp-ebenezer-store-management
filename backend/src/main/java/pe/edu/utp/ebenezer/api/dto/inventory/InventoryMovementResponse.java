package pe.edu.utp.ebenezer.api.dto.inventory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import pe.edu.utp.ebenezer.domain.enums.InventoryMovementType;

public record InventoryMovementResponse(
        Long id,
        Long productId,
        String productName,
        Long lotId,
        Long userId,
        String userName,
        InventoryMovementType movementType,
        BigDecimal baseQuantity,
        Long purchaseDetailId,
        Long saleDetailId,
        Long internalConsumptionDetailId,
        LocalDateTime movementDate,
        String reason,
        String notes
) {
}
