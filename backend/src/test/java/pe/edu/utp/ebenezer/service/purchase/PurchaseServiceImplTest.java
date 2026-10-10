package pe.edu.utp.ebenezer.service.purchase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.utp.ebenezer.api.dto.purchase.PurchaseDetailRequest;
import pe.edu.utp.ebenezer.api.dto.purchase.PurchaseRequest;
import pe.edu.utp.ebenezer.api.dto.purchase.PurchaseResponse;
import pe.edu.utp.ebenezer.domain.entity.InventoryMovement;
import pe.edu.utp.ebenezer.domain.entity.Lot;
import pe.edu.utp.ebenezer.domain.entity.Product;
import pe.edu.utp.ebenezer.domain.entity.Purchase;
import pe.edu.utp.ebenezer.domain.entity.UnitOfMeasure;
import pe.edu.utp.ebenezer.domain.entity.User;
import pe.edu.utp.ebenezer.domain.enums.InventoryMovementType;
import pe.edu.utp.ebenezer.domain.enums.PurchaseStatus;
import pe.edu.utp.ebenezer.domain.enums.UnitType;
import pe.edu.utp.ebenezer.domain.repository.inventory.InventoryMovementRepository;
import pe.edu.utp.ebenezer.domain.repository.inventory.LotRepository;
import pe.edu.utp.ebenezer.domain.repository.product.ProductRepository;
import pe.edu.utp.ebenezer.domain.repository.purchase.PurchaseRepository;
import pe.edu.utp.ebenezer.domain.repository.supplier.SupplierRepository;
import pe.edu.utp.ebenezer.domain.repository.unit.UnitOfMeasureRepository;
import pe.edu.utp.ebenezer.exception.BusinessException;
import pe.edu.utp.ebenezer.security.CurrentUserProvider;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceImplTest {

    @Mock
    private PurchaseRepository purchaseRepository;
    @Mock
    private LotRepository lotRepository;
    @Mock
    private InventoryMovementRepository inventoryMovementRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UnitOfMeasureRepository unitOfMeasureRepository;
    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private PurchaseServiceImpl purchaseService;

    private final UnitOfMeasure kilogram = unit(1L, "KG", UnitType.WEIGHT, "1");
    private final UnitOfMeasure gram = unit(2L, "G", UnitType.WEIGHT, "0.001");
    private final UnitOfMeasure piece = unit(3L, "UND", UnitType.UNIT, "1");

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);
        user.setName("Rosa");
        when(currentUserProvider.getCurrentUser()).thenReturn(user);
    }

    @Test
    void createConvertsQuantityToBaseUnitAndRegistersLotAndMovement() {
        Product rice = product(10L, kilogram);
        when(productRepository.findById(10L)).thenReturn(Optional.of(rice));
        when(unitOfMeasureRepository.findById(2L)).thenReturn(Optional.of(gram));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(invocation -> invocation.getArgument(0));
        LocalDate expiration = LocalDate.now().plusDays(30);

        PurchaseResponse response = purchaseService.create(new PurchaseRequest(null, null, null, List.of(
                new PurchaseDetailRequest(10L, 2L, new BigDecimal("500"), new BigDecimal("2.10"), null,
                        " L-01 ", expiration))));

        assertThat(response.status()).isEqualTo(PurchaseStatus.REGISTERED);
        assertThat(response.total()).isEqualByComparingTo("2.10");
        assertThat(response.details().getFirst().baseQuantity()).isEqualByComparingTo("0.500");
        assertThat(response.details().getFirst().unitCost()).isEqualByComparingTo("0.0042");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Lot>> lots = ArgumentCaptor.forClass(List.class);
        verify(lotRepository).saveAll(lots.capture());
        assertThat(lots.getValue().getFirst().getLotCode()).isEqualTo("L-01");
        assertThat(lots.getValue().getFirst().getExpirationDate()).isEqualTo(expiration);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<InventoryMovement>> movements = ArgumentCaptor.forClass(List.class);
        verify(inventoryMovementRepository).saveAll(movements.capture());
        InventoryMovement movement = movements.getValue().getFirst();
        assertThat(movement.getMovementType()).isEqualTo(InventoryMovementType.PURCHASE);
        assertThat(movement.getBaseQuantity()).isEqualByComparingTo("0.500");
        assertThat(movement.getLot()).isSameAs(lots.getValue().getFirst());
    }

    @Test
    void createUpdatesSalePriceWhenPresent() {
        Product water = product(11L, piece);
        when(productRepository.findById(11L)).thenReturn(Optional.of(water));
        when(unitOfMeasureRepository.findById(3L)).thenReturn(Optional.of(piece));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        purchaseService.create(new PurchaseRequest(null, null, null, List.of(
                new PurchaseDetailRequest(11L, 3L, new BigDecimal("24"), new BigDecimal("20.40"),
                        new BigDecimal("1.50"), null, null))));

        assertThat(water.getSalePrice()).isEqualByComparingTo("1.50");
    }

    @Test
    void createRejectsUnitIncompatibleWithBaseUnit() {
        when(productRepository.findById(11L)).thenReturn(Optional.of(product(11L, piece)));
        when(unitOfMeasureRepository.findById(2L)).thenReturn(Optional.of(gram));

        assertThatThrownBy(() -> purchaseService.create(new PurchaseRequest(null, null, null, List.of(
                new PurchaseDetailRequest(11L, 2L, BigDecimal.TEN, BigDecimal.ONE, null, null, null)))))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Unit G is not compatible with product base unit UND");
        verify(purchaseRepository, never()).save(any());
        verify(inventoryMovementRepository, never()).saveAll(anyList());
    }

    private static UnitOfMeasure unit(Long id, String abbreviation, UnitType type, String factor) {
        UnitOfMeasure unit = new UnitOfMeasure();
        unit.setId(id);
        unit.setAbbreviation(abbreviation);
        unit.setType(type);
        unit.setConversionFactor(new BigDecimal(factor));
        return unit;
    }

    private static Product product(Long id, UnitOfMeasure baseUnit) {
        Product product = new Product();
        product.setId(id);
        product.setName("Product " + id);
        product.setBaseUnit(baseUnit);
        product.setSalePrice(BigDecimal.ONE);
        return product;
    }
}
