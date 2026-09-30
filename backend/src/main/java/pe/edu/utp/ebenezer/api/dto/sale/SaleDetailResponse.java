package pe.edu.utp.ebenezer.api.dto.sale;

import java.math.BigDecimal;

public record SaleDetailResponse(
        Long id,
        Long productId,
        String productName,
        Long unitOfMeasureId,
        String unitOfMeasureAbbreviation,
        BigDecimal quantity,
        BigDecimal baseQuantity,
        BigDecimal unitPrice,
        BigDecimal discount,
        BigDecimal subtotal
) {
}
