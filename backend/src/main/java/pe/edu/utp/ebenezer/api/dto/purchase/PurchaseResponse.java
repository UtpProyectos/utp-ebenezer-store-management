package pe.edu.utp.ebenezer.api.dto.purchase;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import pe.edu.utp.ebenezer.domain.enums.PurchaseStatus;

public record PurchaseResponse(
        Long id,
        Long supplierId,
        String supplierName,
        Long userId,
        String userName,
        LocalDateTime purchaseDate,
        BigDecimal subtotal,
        BigDecimal total,
        PurchaseStatus status,
        String notes,
        List<PurchaseDetailResponse> details
) {
}
