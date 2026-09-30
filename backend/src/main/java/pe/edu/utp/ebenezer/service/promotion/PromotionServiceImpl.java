package pe.edu.utp.ebenezer.service.promotion;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.domain.repository.product.ProductRepository;
import pe.edu.utp.ebenezer.domain.repository.product.PromotionRepository;
import pe.edu.utp.ebenezer.domain.repository.unit.UnitOfMeasureRepository;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
}
