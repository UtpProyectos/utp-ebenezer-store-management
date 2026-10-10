package pe.edu.utp.ebenezer.service.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.utp.ebenezer.domain.entity.Lot;
import pe.edu.utp.ebenezer.domain.entity.Product;
import pe.edu.utp.ebenezer.domain.repository.inventory.InventoryMovementRepository;
import pe.edu.utp.ebenezer.domain.repository.inventory.LotRepository;
import pe.edu.utp.ebenezer.domain.repository.inventory.LotStockView;
import pe.edu.utp.ebenezer.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class StockAllocatorTest {

    @Mock
    private InventoryMovementRepository inventoryMovementRepository;
    @Mock
    private LotRepository lotRepository;

    @InjectMocks
    private StockAllocator stockAllocator;

    @Test
    void allocateSkipsExpiredLotsAndEndsWithStockWithoutLot() {
        Product yogurt = product();
        when(inventoryMovementRepository.findLotsWithStockByProductId(7L)).thenReturn(List.of(
                lot(70L, LocalDate.now().minusDays(1), "5"),
                lot(71L, LocalDate.now().plusDays(3), "2"),
                lot(72L, null, "1")));
        when(inventoryMovementRepository.sumUnlottedBaseQuantityByProductId(7L)).thenReturn(new BigDecimal("4"));
        when(lotRepository.getReferenceById(71L)).thenReturn(lotEntity(71L));
        when(lotRepository.getReferenceById(72L)).thenReturn(lotEntity(72L));

        List<StockAllocation> allocations = stockAllocator.allocate(yogurt, new BigDecimal("5"), false);

        assertThat(allocations).hasSize(3);
        assertThat(allocations.get(0).lot().getId()).isEqualTo(71L);
        assertThat(allocations.get(0).quantity()).isEqualByComparingTo("2");
        assertThat(allocations.get(1).lot().getId()).isEqualTo(72L);
        assertThat(allocations.get(1).quantity()).isEqualByComparingTo("1");
        assertThat(allocations.get(2).lot()).isNull();
        assertThat(allocations.get(2).quantity()).isEqualByComparingTo("2");
    }

    @Test
    void allocateRejectsWhenOnlyExpiredStockIsLeft() {
        when(inventoryMovementRepository.findLotsWithStockByProductId(7L)).thenReturn(List.of(
                lot(70L, LocalDate.now().minusDays(1), "5")));
        when(inventoryMovementRepository.sumUnlottedBaseQuantityByProductId(7L)).thenReturn(BigDecimal.ZERO);

        assertThatThrownBy(() -> stockAllocator.allocate(product(), BigDecimal.ONE, false))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Not enough stock for Yogurt");
    }

    @Test
    void allocateIncludesExpiredLotsFirstWhenRequested() {
        when(inventoryMovementRepository.findLotsWithStockByProductId(7L)).thenReturn(List.of(
                lot(70L, LocalDate.now().minusDays(1), "5")));
        when(inventoryMovementRepository.sumUnlottedBaseQuantityByProductId(7L)).thenReturn(BigDecimal.ZERO);
        when(lotRepository.getReferenceById(70L)).thenReturn(lotEntity(70L));

        List<StockAllocation> allocations = stockAllocator.allocate(product(), new BigDecimal("3"), true);

        assertThat(allocations).singleElement().satisfies(allocation -> {
            assertThat(allocation.lot().getId()).isEqualTo(70L);
            assertThat(allocation.quantity()).isEqualByComparingTo("3");
        });
    }

    private static Product product() {
        Product product = new Product();
        product.setId(7L);
        product.setName("Yogurt");
        return product;
    }

    private static Lot lotEntity(Long id) {
        Lot lot = new Lot();
        lot.setId(id);
        return lot;
    }

    private static LotStockView lot(Long lotId, LocalDate expirationDate, String stock) {
        return new LotStockView() {
            @Override
            public Long getLotId() {
                return lotId;
            }

            @Override
            public Long getProductId() {
                return 7L;
            }

            @Override
            public LocalDate getExpirationDate() {
                return expirationDate;
            }

            @Override
            public BigDecimal getStock() {
                return new BigDecimal(stock);
            }
        };
    }
}
