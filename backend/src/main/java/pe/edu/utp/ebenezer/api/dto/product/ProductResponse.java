package pe.edu.utp.ebenezer.api.dto.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        Long categoryId,
        String categoryName,
        Long baseUnitId,
        String baseUnitAbbreviation,
        String name,
        String description,
        String barcode,
        BigDecimal salePrice,
        BigDecimal minStock,
        Boolean active,
        LocalDateTime createdAt,
        BigDecimal currentStock
) {
}
