package pe.edu.utp.ebenezer.api.dto.inventory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// currentStock is derived from inventory movements, never stored.
public record LotResponse(
        Long id,
        Long productId,
        String productName,
        Long purchaseDetailId,
        String lotCode,
        LocalDateTime entryDate,
        LocalDate expirationDate,
        BigDecimal currentStock
) {
}
