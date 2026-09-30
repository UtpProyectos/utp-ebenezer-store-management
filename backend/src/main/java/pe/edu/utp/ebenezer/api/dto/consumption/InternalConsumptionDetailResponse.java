package pe.edu.utp.ebenezer.api.dto.consumption;

import java.math.BigDecimal;

public record InternalConsumptionDetailResponse(
        Long id,
        Long productId,
        String productName,
        Long unitOfMeasureId,
        String unitOfMeasureAbbreviation,
        BigDecimal quantity,
        BigDecimal baseQuantity
) {
}
