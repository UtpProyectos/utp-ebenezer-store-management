package pe.edu.utp.ebenezer.service.sale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.utp.ebenezer.api.dto.sale.SaleDetailRequest;
import pe.edu.utp.ebenezer.api.dto.sale.SaleRequest;
import pe.edu.utp.ebenezer.api.dto.sale.SaleResponse;
import pe.edu.utp.ebenezer.domain.entity.InventoryMovement;
import pe.edu.utp.ebenezer.domain.entity.Lot;
import pe.edu.utp.ebenezer.domain.entity.Product;
import pe.edu.utp.ebenezer.domain.entity.Promotion;
import pe.edu.utp.ebenezer.domain.entity.Sale;
import pe.edu.utp.ebenezer.domain.entity.SaleHistory;
import pe.edu.utp.ebenezer.domain.entity.UnitOfMeasure;
import pe.edu.utp.ebenezer.domain.entity.User;
import pe.edu.utp.ebenezer.domain.enums.InventoryMovementType;
import pe.edu.utp.ebenezer.domain.enums.PaymentMethod;
import pe.edu.utp.ebenezer.domain.enums.SaleHistoryAction;
import pe.edu.utp.ebenezer.domain.enums.SaleStatus;
import pe.edu.utp.ebenezer.domain.enums.UnitType;
import pe.edu.utp.ebenezer.domain.repository.inventory.InventoryMovementRepository;
import pe.edu.utp.ebenezer.domain.repository.product.ProductRepository;
import pe.edu.utp.ebenezer.domain.repository.sale.SaleHistoryRepository;
import pe.edu.utp.ebenezer.domain.repository.sale.SaleRepository;
import pe.edu.utp.ebenezer.domain.repository.unit.UnitOfMeasureRepository;
import pe.edu.utp.ebenezer.exception.BusinessException;
import pe.edu.utp.ebenezer.security.CurrentUserProvider;
import pe.edu.utp.ebenezer.service.inventory.StockAllocation;
import pe.edu.utp.ebenezer.service.inventory.StockAllocator;
import pe.edu.utp.ebenezer.service.promotion.PromotionService;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class SaleServiceImplTest {

    @Mock
    private SaleRepository saleRepository;
    @Mock
    private SaleHistoryRepository saleHistoryRepository;
    @Mock
    private InventoryMovementRepository inventoryMovementRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UnitOfMeasureRepository unitOfMeasureRepository;
    @Mock
    private PromotionService promotionService;
    @Mock
    private StockAllocator stockAllocator;
    @Mock
    private CurrentUserProvider currentUserProvider;

    private SaleServiceImpl saleService;

    private final UnitOfMeasure und = unit(1L, "UND", UnitType.UNIT, "1");
    private final UnitOfMeasure kg = unit(2L, "KG", UnitType.WEIGHT, "1");
    private final UnitOfMeasure g = unit(3L, "G", UnitType.WEIGHT, "0.001");

    @BeforeEach
    void setUp() {
        saleService = new SaleServiceImpl(saleRepository, saleHistoryRepository, inventoryMovementRepository,
                productRepository, unitOfMeasureRepository, promotionService, stockAllocator,
                currentUserProvider, JsonMapper.builder().build());
    }

    @Test
    void createPricesWithPromotionAllocatesStockAndRecordsHistory() {
        Product cookies = product(1L, "Galletas", "0.80", und);
        Product soda = product(2L, "Gaseosa", "2.50", und);
        stubProducts(cookies, soda);
        stubUnits(und);
        when(currentUserProvider.getCurrentUser()).thenReturn(user());
        when(promotionService.findCurrentByProduct(any(LocalDateTime.class)))
                .thenReturn(Map.of(1L, promotion(und, "3", "2.00")));
        when(stockAllocator.allocate(cookies, new BigDecimal("7.000"), false)).thenReturn(List.of(
                new StockAllocation(lot(10L), new BigDecimal("5")), new StockAllocation(null, new BigDecimal("2"))));
        when(stockAllocator.allocate(soda, new BigDecimal("2.000"), false)).thenReturn(List.of(
                new StockAllocation(lot(20L), new BigDecimal("2"))));
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> withId(invocation.getArgument(0)));

        SaleResponse response = saleService.create(new SaleRequest(PaymentMethod.CASH, null, List.of(
                new SaleDetailRequest(1L, 1L, new BigDecimal("7"), null),
                new SaleDetailRequest(2L, 1L, new BigDecimal("2"), null))));

        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.status()).isEqualTo(SaleStatus.CONFIRMED);
        assertThat(response.subtotal()).isEqualByComparingTo("9.80");
        assertThat(response.total()).isEqualByComparingTo("9.80");
        assertThat(response.details().get(0).discount()).isEqualByComparingTo("0.80");
        assertThat(response.details().get(0).subtotal()).isEqualByComparingTo("4.80");
        assertThat(response.details().get(1).subtotal()).isEqualByComparingTo("5.00");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<InventoryMovement>> movements = ArgumentCaptor.forClass(List.class);
        verify(inventoryMovementRepository).saveAll(movements.capture());
        assertThat(movements.getValue()).hasSize(3)
                .allMatch(movement -> movement.getMovementType() == InventoryMovementType.SALE
                        && movement.getBaseQuantity().signum() < 0
                        && movement.getSaleDetail() != null);
        assertThat(movements.getValue().get(1).getLot()).isNull();
        assertThat(movements.getValue().get(1).getBaseQuantity()).isEqualByComparingTo("-2");

        ArgumentCaptor<SaleHistory> history = ArgumentCaptor.forClass(SaleHistory.class);
        verify(saleHistoryRepository).save(history.capture());
        assertThat(history.getValue().getAction()).isEqualTo(SaleHistoryAction.CREATED);
        assertThat(history.getValue().getNewData()).contains("\"total\":9.8");
    }

    @Test
    void createConvertsGramsToTheKilogramBaseUnit() {
        Product rice = product(3L, "Arroz a granel", "4.20", kg);
        stubProducts(rice);
        stubUnits(g);
        when(currentUserProvider.getCurrentUser()).thenReturn(user());
        when(promotionService.findCurrentByProduct(any(LocalDateTime.class))).thenReturn(Map.of());
        when(stockAllocator.allocate(rice, new BigDecimal("0.500"), false)).thenReturn(List.of(
                new StockAllocation(lot(30L), new BigDecimal("0.500"))));
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> withId(invocation.getArgument(0)));

        SaleResponse response = saleService.create(new SaleRequest(PaymentMethod.YAPE_PLIN, null, List.of(
                new SaleDetailRequest(3L, 3L, new BigDecimal("500"), null))));

        assertThat(response.details().getFirst().baseQuantity()).isEqualByComparingTo("0.500");
        assertThat(response.details().getFirst().unitPrice()).isEqualByComparingTo("0.0042");
        assertThat(response.total()).isEqualByComparingTo("2.10");
    }

    @Test
    void createRejectsRepeatedProducts() {
        assertThatThrownBy(() -> saleService.create(new SaleRequest(PaymentMethod.CASH, null, List.of(
                new SaleDetailRequest(1L, 1L, BigDecimal.ONE, null),
                new SaleDetailRequest(1L, 1L, BigDecimal.TWO, null)))))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Product is repeated in the sale");
        verify(saleRepository, never()).save(any());
    }

    @Test
    void createRejectsDiscountAboveSubtotal() {
        Product soda = product(2L, "Gaseosa", "2.50", und);
        stubProducts(soda);
        stubUnits(und);
        when(currentUserProvider.getCurrentUser()).thenReturn(user());
        when(promotionService.findCurrentByProduct(any(LocalDateTime.class))).thenReturn(Map.of());
        when(stockAllocator.allocate(eq(soda), any(BigDecimal.class), eq(false))).thenReturn(List.of());

        assertThatThrownBy(() -> saleService.create(new SaleRequest(PaymentMethod.CASH, new BigDecimal("3"), List.of(
                new SaleDetailRequest(2L, 1L, BigDecimal.ONE, null)))))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Discount cannot be greater than the subtotal");
        verify(saleRepository, never()).save(any());
    }

    @Test
    void createDoesNotSaveWhenStockIsNotEnough() {
        Product soda = product(2L, "Gaseosa", "2.50", und);
        stubProducts(soda);
        stubUnits(und);
        when(currentUserProvider.getCurrentUser()).thenReturn(user());
        when(promotionService.findCurrentByProduct(any(LocalDateTime.class))).thenReturn(Map.of());
        when(stockAllocator.allocate(eq(soda), any(BigDecimal.class), eq(false)))
                .thenThrow(new BusinessException("Not enough stock for Gaseosa"));

        assertThatThrownBy(() -> saleService.create(new SaleRequest(PaymentMethod.CASH, null, List.of(
                new SaleDetailRequest(2L, 1L, new BigDecimal("50"), null)))))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Not enough stock for Gaseosa");
        verify(saleRepository, never()).save(any());
        verify(inventoryMovementRepository, never()).saveAll(anyList());
    }

    private void stubProducts(Product... products) {
        for (Product product : products) {
            when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        }
    }

    private void stubUnits(UnitOfMeasure... units) {
        for (UnitOfMeasure unit : units) {
            when(unitOfMeasureRepository.findById(unit.getId())).thenReturn(Optional.of(unit));
        }
    }

    private static Sale withId(Sale sale) {
        sale.setId(99L);
        return sale;
    }

    private static Product product(Long id, String name, String price, UnitOfMeasure baseUnit) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setSalePrice(new BigDecimal(price));
        product.setBaseUnit(baseUnit);
        product.setActive(true);
        return product;
    }

    private static Promotion promotion(UnitOfMeasure unit, String quantity, String price) {
        Promotion promotion = new Promotion();
        promotion.setUnitOfMeasure(unit);
        promotion.setPromotionQuantity(new BigDecimal(quantity));
        promotion.setPromotionalPrice(new BigDecimal(price));
        promotion.setRepeatable(true);
        return promotion;
    }

    private static UnitOfMeasure unit(Long id, String abbreviation, UnitType type, String factor) {
        UnitOfMeasure unit = new UnitOfMeasure();
        unit.setId(id);
        unit.setAbbreviation(abbreviation);
        unit.setType(type);
        unit.setConversionFactor(new BigDecimal(factor));
        return unit;
    }

    private static Lot lot(Long id) {
        Lot lot = new Lot();
        lot.setId(id);
        return lot;
    }

    private static User user() {
        User user = new User();
        user.setId(1L);
        user.setName("Rosa");
        return user;
    }
}
