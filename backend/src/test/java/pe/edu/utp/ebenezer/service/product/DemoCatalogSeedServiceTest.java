package pe.edu.utp.ebenezer.service.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.utp.ebenezer.domain.repository.category.CategoryRepository;
import pe.edu.utp.ebenezer.domain.repository.inventory.InventoryMovementRepository;
import pe.edu.utp.ebenezer.domain.repository.product.ProductRepository;
import pe.edu.utp.ebenezer.domain.repository.unit.UnitOfMeasureRepository;
import pe.edu.utp.ebenezer.domain.repository.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class DemoCatalogSeedServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UnitOfMeasureRepository unitOfMeasureRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private InventoryMovementRepository inventoryMovementRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DemoCatalogSeedService demoCatalogSeedService;

    @Test
    void doesNotSeedWhenProductsAlreadyExist() {
        when(productRepository.count()).thenReturn(1L);

        assertThat(demoCatalogSeedService.seedIfCatalogIsEmpty()).isFalse();

        verify(categoryRepository, never()).findByName(org.mockito.ArgumentMatchers.anyString());
        verify(unitOfMeasureRepository, never()).findByAbbreviation("UND");
        verify(userRepository, never()).findFirstByRole_NameAndActiveTrueOrderByIdAsc(
                org.mockito.ArgumentMatchers.any());
    }
}
