package pe.edu.utp.ebenezer.service.inventory;

import pe.edu.utp.ebenezer.api.dto.inventory.InventoryMovementResponse;
import pe.edu.utp.ebenezer.domain.entity.InventoryMovement;

public final class InventoryMapper {

    private InventoryMapper() {
    }

    public static InventoryMovementResponse toResponse(InventoryMovement movement) {
        return new InventoryMovementResponse(
                movement.getId(),
                movement.getProduct().getId(),
                movement.getProduct().getName(),
                movement.getLot() == null ? null : movement.getLot().getId(),
                movement.getUser().getId(),
                movement.getUser().getName(),
                movement.getMovementType(),
                movement.getBaseQuantity(),
                movement.getPurchaseDetail() == null ? null : movement.getPurchaseDetail().getId(),
                movement.getSaleDetail() == null ? null : movement.getSaleDetail().getId(),
                movement.getInternalConsumptionDetail() == null ? null : movement.getInternalConsumptionDetail().getId(),
                movement.getMovementDate(),
                movement.getReason(),
                movement.getNotes());
    }
}
