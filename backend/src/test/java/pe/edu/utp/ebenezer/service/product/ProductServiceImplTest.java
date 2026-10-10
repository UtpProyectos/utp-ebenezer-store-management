package pe.edu.utp.ebenezer.service.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

import pe.edu.utp.ebenezer.api.dto.product.ProductRequest;
import pe.edu.utp.ebenezer.domain.entity.Category;
import pe.edu.utp.ebenezer.domain.entity.Product;
import pe.edu.utp.ebenezer.domain.entity.UnitOfMeasure;
import pe.edu.utp.ebenezer.domain.repository.category.CategoryRepository;
import pe.edu.utp.ebenezer.domain.repository.inventory.InventoryMovementRepository;
import pe.edu.utp.ebenezer.domain.repository.product.ProductRepository;
import pe.edu.utp.ebenezer.domain.repository.unit.UnitOfMeasureRepository;
import pe.edu.utp.ebenezer.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UnitOfMeasureRepository unitOfMeasureRepository;
    @Mock
    private InventoryMovementRepository inventoryMovementRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void createTrimsProductNameAndReturnsDerivedStock() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Bebidas");
        category.setActive(true);
        UnitOfMeasure unit = new UnitOfMeasure();
        unit.setId(1L);
        unit.setAbbreviation("UND");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(unitOfMeasureRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(7L);
            return product;
        });
        when(inventoryMovementRepository.sumBaseQuantityByProductId(7L)).thenReturn(BigDecimal.ZERO);

        var response = productService.create(new ProductRequest(
                1L,
                1L,
                "  Agua mineral  ",
                null,
                null,
                new BigDecimal("1.50"),
                null
        ));

        assertThat(response.name()).isEqualTo("Agua mineral");
        assertThat(response.currentStock()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.minStock()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void createRejectsDuplicateBarcode() {
        Category category = new Category();
        category.setActive(true);
        UnitOfMeasure unit = new UnitOfMeasure();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(unitOfMeasureRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(productRepository.existsByBarcode("123")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(new ProductRequest(
                1L,
                1L,
                "Agua mineral",
                null,
                " 123 ",
                new BigDecimal("1.50"),
                BigDecimal.ZERO
        ))).isInstanceOf(BusinessException.class).hasMessage("Barcode is already in use");

        verify(productRepository, never()).save(any(Product.class));
    }
}
