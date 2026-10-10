package pe.edu.utp.ebenezer.service.consumption;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.utp.ebenezer.api.dto.consumption.InternalConsumptionDetailRequest;
import pe.edu.utp.ebenezer.api.dto.consumption.InternalConsumptionRequest;
import pe.edu.utp.ebenezer.api.dto.consumption.InternalConsumptionResponse;
import pe.edu.utp.ebenezer.domain.entity.InternalConsumption;
import pe.edu.utp.ebenezer.domain.entity.InventoryMovement;
import pe.edu.utp.ebenezer.domain.entity.Lot;
import pe.edu.utp.ebenezer.domain.entity.Product;
import pe.edu.utp.ebenezer.domain.entity.UnitOfMeasure;
import pe.edu.utp.ebenezer.domain.entity.User;
import pe.edu.utp.ebenezer.domain.enums.InventoryMovementType;
import pe.edu.utp.ebenezer.domain.enums.UnitType;
import pe.edu.utp.ebenezer.domain.repository.consumption.InternalConsumptionRepository;
import pe.edu.utp.ebenezer.domain.repository.inventory.InventoryMovementRepository;
import pe.edu.utp.ebenezer.domain.repository.product.ProductRepository;
import pe.edu.utp.ebenezer.domain.repository.unit.UnitOfMeasureRepository;
import pe.edu.utp.ebenezer.exception.BusinessException;
import pe.edu.utp.ebenezer.security.CurrentUserProvider;
import pe.edu.utp.ebenezer.service.inventory.StockAllocation;
import pe.edu.utp.ebenezer.service.inventory.StockAllocator;

@ExtendWith(MockitoExtension.class)
class InternalConsumptionServiceImplTest {

    @Mock
    private InternalConsumptionRepository internalConsumptionRepository;
    @Mock
    private InventoryMovementRepository inventoryMovementRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UnitOfMeasureRepository unitOfMeasureRepository;
    @Mock
    private StockAllocator stockAllocator;
    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private InternalConsumptionServiceImpl internalConsumptionService;

    @Test
    void createRegistersNegativeInternalConsumptionMovementsPerLot() {
        UnitOfMeasure und = new UnitOfMeasure();
        und.setId(1L);
        und.setAbbreviation("UND");
        und.setType(UnitType.UNIT);
        und.setConversionFactor(BigDecimal.ONE);
        Product milk = new Product();
        milk.setId(5L);
        milk.setName("Leche");
        milk.setBaseUnit(und);
        milk.setActive(true);
        Lot lot = new Lot();
        lot.setId(50L);

        User user = new User();
        user.setId(1L);
        user.setName("Rosa");
        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(productRepository.findById(5L)).thenReturn(Optional.of(milk));
        when(unitOfMeasureRepository.findById(1L)).thenReturn(Optional.of(und));
        when(stockAllocator.allocate(milk, new BigDecimal("2.000"), false)).thenReturn(List.of(
                new StockAllocation(lot, BigDecimal.ONE), new StockAllocation(null, BigDecimal.ONE)));
        when(internalConsumptionRepository.save(any(InternalConsumption.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InternalConsumptionResponse response = internalConsumptionService.create(new InternalConsumptionRequest(
                null, "  ", null, List.of(new InternalConsumptionDetailRequest(5L, 1L, new BigDecimal("2")))));

        assertThat(response.consumptionDate()).isNotNull();
        assertThat(response.reason()).isNull();
        assertThat(response.details()).singleElement()
                .satisfies(detail -> assertThat(detail.baseQuantity()).isEqualByComparingTo("2"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<InventoryMovement>> movements = ArgumentCaptor.forClass(List.class);
        verify(inventoryMovementRepository).saveAll(movements.capture());
        assertThat(movements.getValue()).hasSize(2)
                .allMatch(movement -> movement.getMovementType() == InventoryMovementType.INTERNAL_CONSUMPTION
                        && movement.getBaseQuantity().compareTo(BigDecimal.ONE.negate()) == 0
                        && movement.getInternalConsumptionDetail() != null);
    }

    @Test
    void createRejectsRepeatedProducts() {
        assertThatThrownBy(() -> internalConsumptionService.create(new InternalConsumptionRequest(null, null, null,
                List.of(new InternalConsumptionDetailRequest(5L, 1L, BigDecimal.ONE),
                        new InternalConsumptionDetailRequest(5L, 1L, BigDecimal.ONE)))))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Product is repeated in the consumption");
        verify(internalConsumptionRepository, never()).save(any());
    }
}
