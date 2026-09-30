package pe.edu.utp.ebenezer.api.dto.purchase;

import java.math.BigDecimal;

public record PurchaseDetailResponse(
        Long id,
        Long productId,
        String productName,
        Long unitOfMeasureId,
        String unitOfMeasureAbbreviation,
        BigDecimal quantity,
        BigDecimal baseQuantity,
        BigDecimal unitCost,
        BigDecimal subtotal
) {
}
