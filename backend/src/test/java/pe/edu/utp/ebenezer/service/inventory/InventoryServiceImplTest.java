package pe.edu.utp.ebenezer.service.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.utp.ebenezer.api.dto.inventory.InventoryMovementRequest;
import pe.edu.utp.ebenezer.api.dto.inventory.InventoryMovementResponse;
import pe.edu.utp.ebenezer.api.dto.inventory.ProductStockResponse;
import pe.edu.utp.ebenezer.api.dto.inventory.StockStatus;
import pe.edu.utp.ebenezer.domain.entity.Category;
import pe.edu.utp.ebenezer.domain.entity.Lot;
import pe.edu.utp.ebenezer.domain.entity.Product;
import pe.edu.utp.ebenezer.domain.entity.UnitOfMeasure;
import pe.edu.utp.ebenezer.domain.entity.User;
import pe.edu.utp.ebenezer.domain.enums.InventoryMovementType;
import pe.edu.utp.ebenezer.domain.enums.PurchaseStatus;
import pe.edu.utp.ebenezer.domain.enums.UnitType;
import pe.edu.utp.ebenezer.domain.repository.inventory.InventoryMovementRepository;
import pe.edu.utp.ebenezer.domain.repository.inventory.LotRepository;
import pe.edu.utp.ebenezer.domain.repository.inventory.LotStockView;
import pe.edu.utp.ebenezer.domain.repository.inventory.ProductStockView;
import pe.edu.utp.ebenezer.domain.repository.product.ProductRepository;
import pe.edu.utp.ebenezer.domain.repository.purchase.PurchaseDetailRepository;
import pe.edu.utp.ebenezer.domain.repository.settings.BusinessSettingsRepository;
import pe.edu.utp.ebenezer.exception.BusinessException;
import pe.edu.utp.ebenezer.security.CurrentUserProvider;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryMovementRepository inventoryMovementRepository;
    @Mock
    private LotRepository lotRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private PurchaseDetailRepository purchaseDetailRepository;
    @Mock
    private BusinessSettingsRepository businessSettingsRepository;
    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @Test
    void findStockResolvesStatusWithPrototypePriority() {
        LocalDate today = LocalDate.now();
        List<Product> products = List.of(
                product(1L, "10"),  // ok
                product(2L, "10"),  // low: 8 <= 10
                product(3L, "10"),  // critical: 4 <= 5
                product(4L, "10"),  // expiring soon (20 in stock, lot expires in 3 days)
                product(5L, "10")); // expired lot wins over everything else
        when(productRepository.findByActiveTrueOrderByNameAsc()).thenReturn(products);
        when(businessSettingsRepository.findAll()).thenReturn(List.of());
        when(purchaseDetailRepository.findLatestByProduct(PurchaseStatus.REGISTERED)).thenReturn(List.of());
        when(inventoryMovementRepository.findStockByProduct()).thenReturn(List.of(
                stock(1L, "50"), stock(2L, "8"), stock(3L, "4"), stock(4L, "20"), stock(5L, "50")));
        when(inventoryMovementRepository.findLotsWithStock()).thenReturn(List.of(
                lot(40L, 4L, today.plusDays(3), "20"),
                lot(50L, 5L, today.minusDays(1), "2"),
                lot(51L, 5L, null, "48")));

        Map<Long, ProductStockResponse> result = inventoryService.findStock().stream()
                .collect(Collectors.toMap(ProductStockResponse::productId, Function.identity()));

        assertThat(result.get(1L).status()).isEqualTo(StockStatus.OK);
        assertThat(result.get(2L).status()).isEqualTo(StockStatus.LOW);
        assertThat(result.get(3L).status()).isEqualTo(StockStatus.CRITICAL);
        assertThat(result.get(4L).status()).isEqualTo(StockStatus.EXPIRING_SOON);
        assertThat(result.get(4L).nextExpirationDate()).isEqualTo(today.plusDays(3));
        assertThat(result.get(5L).status()).isEqualTo(StockStatus.EXPIRED);
        assertThat(result.get(5L).expiredQuantity()).isEqualByComparingTo("2");
    }

    @Test
    void registerWithdrawalAllocatesAcrossLotsUsingFefo() {
        Product cheese = product(5L, "6");
        when(productRepository.findById(5L)).thenReturn(Optional.of(cheese));
        when(currentUserProvider.getCurrentUser()).thenReturn(user());
        when(inventoryMovementRepository.findLotsWithStockByProductId(5L)).thenReturn(List.of(
                lot(50L, 5L, LocalDate.now().minusDays(2), "2"),
                lot(51L, 5L, LocalDate.now().plusDays(20), "10")));
        when(lotRepository.getReferenceById(50L)).thenReturn(lotEntity(50L));
        when(lotRepository.getReferenceById(51L)).thenReturn(lotEntity(51L));
        when(inventoryMovementRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<InventoryMovementResponse> movements = inventoryService.registerWithdrawal(new InventoryMovementRequest(
                5L, null, InventoryMovementType.WASTE, new BigDecimal("3"), "EXPIRED", null));

        assertThat(movements).hasSize(2);
        assertThat(movements.get(0).lotId()).isEqualTo(50L);
        assertThat(movements.get(0).baseQuantity()).isEqualByComparingTo("-2");
        assertThat(movements.get(1).lotId()).isEqualTo(51L);
        assertThat(movements.get(1).baseQuantity()).isEqualByComparingTo("-1");
        assertThat(movements).allMatch(movement -> "EXPIRED".equals(movement.reason()));
    }

    @Test
    void registerWithdrawalRejectsQuantityAboveStock() {
        when(productRepository.findById(5L)).thenReturn(Optional.of(product(5L, "6")));
        when(inventoryMovementRepository.findLotsWithStockByProductId(5L)).thenReturn(List.of(
                lot(50L, 5L, null, "2")));

        assertThatThrownBy(() -> inventoryService.registerWithdrawal(new InventoryMovementRequest(
                5L, null, InventoryMovementType.RETURN, new BigDecimal("3"), "EXPIRED", null)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Not enough stock");
        verify(inventoryMovementRepository, never()).saveAll(anyList());
    }

    @Test
    void registerWithdrawalRejectsNonWithdrawalTypes() {
        assertThatThrownBy(() -> inventoryService.registerWithdrawal(new InventoryMovementRequest(
                5L, null, InventoryMovementType.ADJUSTMENT_IN, BigDecimal.ONE, null, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Movement type not allowed: ADJUSTMENT_IN");
    }

    private static Product product(Long id, String minStock) {
        Category category = new Category();
        category.setName("Abarrotes");
        UnitOfMeasure unit = new UnitOfMeasure();
        unit.setId(1L);
        unit.setAbbreviation("UND");
        unit.setType(UnitType.UNIT);

        Product product = new Product();
        product.setId(id);
        product.setName("Product " + id);
        product.setCategory(category);
        product.setBaseUnit(unit);
        product.setSalePrice(BigDecimal.ONE);
        product.setMinStock(new BigDecimal(minStock));
        return product;
    }

    private static User user() {
        User user = new User();
        user.setId(1L);
        user.setName("Rosa");
        return user;
    }

    private static Lot lotEntity(Long id) {
        Lot lot = new Lot();
        lot.setId(id);
        return lot;
    }

    private static ProductStockView stock(Long productId, String stock) {
        return new ProductStockView() {
            @Override
            public Long getProductId() {
                return productId;
            }

            @Override
            public BigDecimal getStock() {
                return new BigDecimal(stock);
            }
        };
    }

    private static LotStockView lot(Long lotId, Long productId, LocalDate expirationDate, String stock) {
        return new LotStockView() {
            @Override
            public Long getLotId() {
                return lotId;
            }

            @Override
            public Long getProductId() {
                return productId;
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
