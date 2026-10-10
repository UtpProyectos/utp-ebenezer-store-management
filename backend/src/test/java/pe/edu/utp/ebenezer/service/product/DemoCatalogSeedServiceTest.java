package pe.edu.utp.ebenezer.service.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
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

import pe.edu.utp.ebenezer.domain.entity.Category;
import pe.edu.utp.ebenezer.domain.entity.Product;
import pe.edu.utp.ebenezer.domain.entity.UnitOfMeasure;
import pe.edu.utp.ebenezer.domain.enums.UnitType;
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
    void doesNotCreateDuplicateProducts() {
        when(categoryRepository.findByName(anyString())).thenAnswer(invocation -> {
            Category category = new Category();
            category.setName(invocation.getArgument(0));
            category.setActive(true);
            return Optional.of(category);
        });
        when(productRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.of(new Product()));

        assertThat(demoCatalogSeedService.seedMissingDemoProducts()).isFalse();

        verify(categoryRepository).findByName("Embutidos");
        verify(unitOfMeasureRepository, never()).findByAbbreviation("UND");
        verify(userRepository, never()).findFirstByRole_NameAndActiveTrueOrderByIdAsc(
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void addsFiveMissingSanFernandoProductsWithoutInventingOpeningStock() {
        when(categoryRepository.findByName(anyString())).thenAnswer(invocation -> {
            Category category = new Category();
            category.setName(invocation.getArgument(0));
            category.setActive(true);
            return Optional.of(category);
        });
        when(productRepository.findByNameIgnoreCase(anyString())).thenAnswer(invocation ->
                invocation.<String>getArgument(0).contains("San Fernando")
                        ? Optional.empty()
                        : Optional.of(new Product()));
        UnitOfMeasure unit = new UnitOfMeasure();
        unit.setType(UnitType.UNIT);
        unit.setConversionFactor(BigDecimal.ONE);
        when(unitOfMeasureRepository.findByAbbreviation("UND")).thenReturn(Optional.of(unit));

        assertThat(demoCatalogSeedService.seedMissingDemoProducts()).isTrue();

        verify(productRepository).saveAll(argThat(products -> {
            var seededProducts = new java.util.ArrayList<Product>();
            products.forEach(seededProducts::add);
            var prices = seededProducts.stream().collect(java.util.stream.Collectors.toMap(
                    Product::getName,
                    product -> product.getSalePrice().toPlainString()
            ));
            return prices.equals(java.util.Map.of(
                    "Hot Dog San Fernando x3", "1.70",
                    "Hot Dog San Fernando x6", "3.20",
                    "Jamonada San Fernando", "1.70",
                    "Chicharrón de prensa San Fernando", "1.60",
                    "Chorizo San Fernando", "1.60"
            )) && seededProducts.stream().allMatch(product -> product.getCategory().getName().equals("Embutidos"));
        }));
        verify(inventoryMovementRepository, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
        verify(userRepository, never()).findFirstByRole_NameAndActiveTrueOrderByIdAsc(
                org.mockito.ArgumentMatchers.any());
    }
}
