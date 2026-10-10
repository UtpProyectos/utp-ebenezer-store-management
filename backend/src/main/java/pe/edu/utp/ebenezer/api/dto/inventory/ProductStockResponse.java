package pe.edu.utp.ebenezer.api.dto.inventory;

import java.math.BigDecimal;
import java.time.LocalDate;

import pe.edu.utp.ebenezer.domain.enums.UnitType;

// Quantities are expressed in the product base unit. lastUnitCost is the cost per base unit of the last purchase.
public record ProductStockResponse(
        Long productId,
        String productName,
        String barcode,
        String categoryName,
        Long baseUnitId,
        String baseUnitAbbreviation,
        UnitType baseUnitType,
        BigDecimal currentStock,
        BigDecimal minStock,
        BigDecimal salePrice,
        BigDecimal lastUnitCost,
        String lastSupplierName,
        LocalDate nextExpirationDate,
        BigDecimal expiredQuantity,
        StockStatus status
) {
}
