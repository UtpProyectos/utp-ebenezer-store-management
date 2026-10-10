package pe.edu.utp.ebenezer.domain.repository.inventory;

import java.math.BigDecimal;
import java.util.List;

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

    // Stock that entered without a lot (e.g. initial adjustments); consumed after every lot.
    @Query("select coalesce(sum(m.baseQuantity), 0) from InventoryMovement m where m.product.id = :productId and m.lot is null")
    BigDecimal sumUnlottedBaseQuantityByProductId(@Param("productId") Long productId);

    @Query("""
            select m.product.id as productId, sum(m.baseQuantity) as stock
            from InventoryMovement m
            group by m.product.id""")
    List<ProductStockView> findStockByProduct();

    @Query("""
            select l.id as lotId, l.product.id as productId, l.expirationDate as expirationDate,
                   sum(m.baseQuantity) as stock
            from InventoryMovement m join m.lot l
            group by l.id, l.product.id, l.expirationDate
            having sum(m.baseQuantity) > 0""")
    List<LotStockView> findLotsWithStock();

    // FEFO order: earliest expiration first, lots without expiration last.
    @Query("""
            select l.id as lotId, l.product.id as productId, l.expirationDate as expirationDate,
                   sum(m.baseQuantity) as stock
            from InventoryMovement m join m.lot l
            where l.product.id = :productId
            group by l.id, l.product.id, l.expirationDate
            having sum(m.baseQuantity) > 0
            order by l.expirationDate asc nulls last, l.id asc""")
    List<LotStockView> findLotsWithStockByProductId(@Param("productId") Long productId);
}
