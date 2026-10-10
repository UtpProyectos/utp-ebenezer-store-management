package pe.edu.utp.ebenezer.service.promotion;

import pe.edu.utp.ebenezer.api.dto.promotion.PromotionResponse;
import pe.edu.utp.ebenezer.domain.entity.Promotion;

public final class PromotionMapper {

    private PromotionMapper() {
    }

    public static PromotionResponse toResponse(Promotion promotion) {
        return new PromotionResponse(
                promotion.getId(),
                promotion.getProduct().getId(),
                promotion.getProduct().getName(),
                promotion.getUnitOfMeasure().getId(),
                promotion.getUnitOfMeasure().getAbbreviation(),
                promotion.getName(),
                promotion.getPromotionQuantity(),
                promotion.getPromotionalPrice(),
                promotion.getRepeatable(),
                promotion.getStartDate(),
                promotion.getEndDate(),
                promotion.getActive());
    }
}
