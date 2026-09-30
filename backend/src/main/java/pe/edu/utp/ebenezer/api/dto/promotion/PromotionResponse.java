package pe.edu.utp.ebenezer.api.dto.promotion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PromotionResponse(
        Long id,
        Long productId,
        String productName,
        Long unitOfMeasureId,
        String unitOfMeasureAbbreviation,
        String name,
        BigDecimal promotionQuantity,
        BigDecimal promotionalPrice,
        Boolean repeatable,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Boolean active
) {
}
