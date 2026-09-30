package pe.edu.utp.ebenezer.domain.repository.inventory;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pe.edu.utp.ebenezer.domain.entity.InventoryMovement;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    // Stock is never stored: it is always the signed sum of movements (see AGENTS/database.md).
    @Query("select coalesce(sum(m.baseQuantity), 0) from InventoryMovement m where m.product.id = :productId")
    BigDecimal sumBaseQuantityByProductId(@Param("productId") Long productId);

    @Query("select coalesce(sum(m.baseQuantity), 0) from InventoryMovement m where m.lot.id = :lotId")
    BigDecimal sumBaseQuantityByLotId(@Param("lotId") Long lotId);
}
