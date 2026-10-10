package pe.edu.utp.ebenezer.service.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import pe.edu.utp.ebenezer.api.dto.category.CategoryRequest;
import pe.edu.utp.ebenezer.domain.entity.Category;
import pe.edu.utp.ebenezer.domain.repository.category.CategoryRepository;
import pe.edu.utp.ebenezer.domain.repository.product.ProductRepository;
import pe.edu.utp.ebenezer.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void createTrimsValuesAndReturnsProductCount() {
        when(categoryRepository.existsByNameIgnoreCase("Bebidas")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(3L);
            return category;
        });
        when(productRepository.countByCategory_Id(3L)).thenReturn(4L);

        var response = categoryService.create(new CategoryRequest(" Bebidas ", "  Gaseosas y jugos  "));

        assertThat(response.name()).isEqualTo("Bebidas");
        assertThat(response.description()).isEqualTo("Gaseosas y jugos");
        assertThat(response.active()).isTrue();
        assertThat(response.productCount()).isEqualTo(4);
    }

    @Test
    void createRejectsDuplicateNameIgnoringCase() {
        when(categoryRepository.existsByNameIgnoreCase("bebidas")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(new CategoryRequest("bebidas", null)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Category name is already in use");
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateAllowsKeepingItsCurrentName() {
        Category category = new Category();
        category.setId(2L);
        category.setName("Bebidas");
        category.setActive(true);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(productRepository.countByCategory_Id(2L)).thenReturn(0L);

        var response = categoryService.update(2L, new CategoryRequest(" bebidas ", null));

        assertThat(response.name()).isEqualTo("bebidas");
        verify(categoryRepository, never()).existsByNameIgnoreCase(any());
    }

    @Test
    void findAllUsesGroupedProductCountsAndDefaultsToZero() {
        Category drinks = category(1L, "Bebidas");
        Category snacks = category(2L, "Snacks");
        when(categoryRepository.findByActive(true, Sort.by("name"))).thenReturn(List.of(drinks, snacks));
        when(productRepository.countByCategory()).thenReturn(List.of(productCount(1L, 5L)));

        var response = categoryService.findAll(true);

        assertThat(response).extracting("name").containsExactly("Bebidas", "Snacks");
        assertThat(response).extracting("productCount").containsExactly(5L, 0L);
        verify(productRepository, never()).countByCategory_Id(any());
    }

    private static Category category(Long id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setActive(true);
        return category;
    }

    private static ProductRepository.CategoryProductCount productCount(Long categoryId, Long count) {
        return new ProductRepository.CategoryProductCount() {
            @Override
            public Long getCategoryId() {
                return categoryId;
            }

            @Override
            public Long getProductCount() {
                return count;
            }
        };
    }
}
