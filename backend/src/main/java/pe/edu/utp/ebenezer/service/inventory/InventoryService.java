package pe.edu.utp.ebenezer.service.inventory;

import java.time.LocalDate;
import java.util.List;

import pe.edu.utp.ebenezer.api.dto.inventory.InventoryMovementRequest;
import pe.edu.utp.ebenezer.api.dto.inventory.InventoryMovementResponse;
import pe.edu.utp.ebenezer.api.dto.inventory.ProductStockResponse;

public interface InventoryService {

    /** Current stock and status of every active product. */
    List<ProductStockResponse> findStock();

    /** Kardex: every movement registered between two days (both included), newest first. */
    List<InventoryMovementResponse> findMovements(LocalDate from, LocalDate to);

    /** Registers a withdrawal (WASTE or RETURN). Produces one negative movement per affected lot. */
    List<InventoryMovementResponse> registerWithdrawal(InventoryMovementRequest request);
}
