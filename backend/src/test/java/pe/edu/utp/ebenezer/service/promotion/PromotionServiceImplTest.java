package pe.edu.utp.ebenezer.service.promotion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.utp.ebenezer.domain.entity.Product;
import pe.edu.utp.ebenezer.domain.entity.Promotion;
import pe.edu.utp.ebenezer.domain.repository.product.PromotionRepository;

@ExtendWith(MockitoExtension.class)
class PromotionServiceImplTest {

    @Mock
    private PromotionRepository promotionRepository;

    @InjectMocks
    private PromotionServiceImpl promotionService;

    @Test
    void findCurrentByProductKeepsTheNewestPromotionOfEachProduct() {
        LocalDateTime now = LocalDateTime.now();
        // The repository returns the newest first.
        when(promotionRepository.findCurrent(now)).thenReturn(List.of(
                promotion(30L, 1L), promotion(20L, 2L), promotion(10L, 1L)));

        Map<Long, Promotion> byProduct = promotionService.findCurrentByProduct(now);

        assertThat(byProduct).hasSize(2);
        assertThat(byProduct.get(1L).getId()).isEqualTo(30L);
        assertThat(byProduct.get(2L).getId()).isEqualTo(20L);
    }

    private static Promotion promotion(Long id, Long productId) {
        Product product = new Product();
        product.setId(productId);
        Promotion promotion = new Promotion();
        promotion.setId(id);
        promotion.setProduct(product);
        return promotion;
    }
}
