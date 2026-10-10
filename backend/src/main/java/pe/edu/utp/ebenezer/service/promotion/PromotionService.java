package pe.edu.utp.ebenezer.service.promotion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import pe.edu.utp.ebenezer.api.dto.promotion.PromotionResponse;
import pe.edu.utp.ebenezer.domain.entity.Promotion;

public interface PromotionService {

    /** Active promotions valid right now, newest first. */
    List<PromotionResponse> findCurrent();

    /**
     * The promotion that applies to each product at {@code now}. When a product has several valid
     * promotions, the most recent one (highest id) wins.
     */
    Map<Long, Promotion> findCurrentByProduct(LocalDateTime now);
}
