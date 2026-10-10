package pe.edu.utp.ebenezer.service.promotion;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.api.dto.promotion.PromotionResponse;
import pe.edu.utp.ebenezer.domain.entity.Promotion;
import pe.edu.utp.ebenezer.domain.repository.product.PromotionRepository;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PromotionResponse> findCurrent() {
        return promotionRepository.findCurrent(LocalDateTime.now()).stream()
                .map(PromotionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Promotion> findCurrentByProduct(LocalDateTime now) {
        Map<Long, Promotion> byProduct = new LinkedHashMap<>();
        // Newest first, so the first promotion seen for a product is the one that applies.
        for (Promotion promotion : promotionRepository.findCurrent(now)) {
            byProduct.putIfAbsent(promotion.getProduct().getId(), promotion);
        }
        return byProduct;
    }
}
