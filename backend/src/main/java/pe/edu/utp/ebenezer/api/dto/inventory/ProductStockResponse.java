package pe.edu.utp.ebenezer.api.dto.inventory;

import java.math.BigDecimal;

public record ProductStockResponse(
        Long productId,
        String productName,
        String baseUnitAbbreviation,
        BigDecimal currentStock,
        BigDecimal minStock
) {
}
