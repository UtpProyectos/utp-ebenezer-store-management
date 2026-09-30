package pe.edu.utp.ebenezer.api.dto.shoppinglist;

import java.math.BigDecimal;

public record ShoppingListDetailResponse(
        Long id,
        Long productId,
        String productName,
        Long supplierId,
        String supplierName,
        Long unitOfMeasureId,
        String unitOfMeasureAbbreviation,
        BigDecimal suggestedQuantity,
        BigDecimal purchasedQuantity,
        BigDecimal estimatedCost,
        Boolean purchased
) {
}
