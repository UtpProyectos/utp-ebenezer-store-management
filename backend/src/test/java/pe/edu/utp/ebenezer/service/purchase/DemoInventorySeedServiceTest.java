package pe.edu.utp.ebenezer.service.purchase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.utp.ebenezer.domain.entity.Product;
import pe.edu.utp.ebenezer.domain.entity.User;
import pe.edu.utp.ebenezer.domain.enums.RoleName;
import pe.edu.utp.ebenezer.domain.repository.category.CategoryRepository;
import pe.edu.utp.ebenezer.domain.repository.inventory.InventoryMovementRepository;
import pe.edu.utp.ebenezer.domain.repository.inventory.LotRepository;
import pe.edu.utp.ebenezer.domain.repository.product.ProductRepository;
import pe.edu.utp.ebenezer.domain.repository.product.PromotionRepository;
import pe.edu.utp.ebenezer.domain.repository.purchase.PurchaseRepository;
import pe.edu.utp.ebenezer.domain.repository.supplier.SupplierRepository;
import pe.edu.utp.ebenezer.domain.repository.unit.UnitOfMeasureRepository;
import pe.edu.utp.ebenezer.domain.repository.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class DemoInventorySeedServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;
    @Mock
    private LotRepository lotRepository;
    @Mock
    private InventoryMovementRepository inventoryMovementRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UnitOfMeasureRepository unitOfMeasureRepository;
    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private PromotionRepository promotionRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DemoInventorySeedService demoInventorySeedService;

    @Test
    void doesNothingWhenDemoPurchasesAlreadyExist() {
        when(unitOfMeasureRepository.findByAbbreviation(anyString())).thenReturn(Optional.empty());
        when(purchaseRepository.existsByNotes(DemoInventorySeedService.SEED_NOTES)).thenReturn(true);

        assertThat(demoInventorySeedService.seedMissingDemoInventory()).isFalse();
        verify(purchaseRepository, never()).save(any());
        verify(promotionRepository, never()).save(any());
    }

    @Test
    void registersPurchasesWithLotsAndMovementsOnFirstRun() {
        Product product = new Product();
        product.setId(1L);
        product.setMinStock(BigDecimal.ZERO);
        when(unitOfMeasureRepository.findByAbbreviation(anyString())).thenReturn(Optional.empty());
        when(purchaseRepository.existsByNotes(DemoInventorySeedService.SEED_NOTES)).thenReturn(false);
        when(userRepository.findFirstByRole_NameAndActiveTrueOrderByIdAsc(RoleName.ADMIN))
                .thenReturn(Optional.of(new User()));
        when(productRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(productRepository.findByNameIgnoreCase("Gaseosa Inca Kola 500 ml")).thenReturn(Optional.of(product));

        assertThat(demoInventorySeedService.seedMissingDemoInventory()).isTrue();
        verify(purchaseRepository).save(any());
        verify(lotRepository).saveAll(any());
        verify(inventoryMovementRepository).saveAll(any());
        assertThat(product.getMinStock()).isEqualByComparingTo("12");
    }
}
