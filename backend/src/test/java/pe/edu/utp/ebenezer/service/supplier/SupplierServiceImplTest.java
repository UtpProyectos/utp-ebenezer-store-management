package pe.edu.utp.ebenezer.service.supplier;

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

import pe.edu.utp.ebenezer.api.dto.supplier.SupplierRequest;
import pe.edu.utp.ebenezer.domain.entity.Product;
import pe.edu.utp.ebenezer.domain.entity.Supplier;
import pe.edu.utp.ebenezer.domain.enums.SupplierType;
import pe.edu.utp.ebenezer.domain.repository.product.ProductRepository;
import pe.edu.utp.ebenezer.domain.repository.supplier.SupplierRepository;
import pe.edu.utp.ebenezer.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class SupplierServiceImplTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    @Test
    void createTrimsAndNormalizesSupplierFields() {
        when(supplierRepository.existsByNameIgnoreCase("Arca Continental")).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier supplier = invocation.getArgument(0);
            supplier.setId(3L);
            return supplier;
        });

        var response = supplierService.create(new SupplierRequest(
                " Arca Continental ",
                null,
                " 981 234 567 ",
                SupplierType.DISTRIBUTOR,
                " Carlos Mendoza ",
                " Planta Moche ",
                " Entrega lunes y jueves ",
                List.of()
        ));

        assertThat(response.name()).isEqualTo("Arca Continental");
        assertThat(response.phone()).isEqualTo("981 234 567");
        assertThat(response.contactName()).isEqualTo("Carlos Mendoza");
        assertThat(response.notes()).isEqualTo("Entrega lunes y jueves");
        assertThat(response.active()).isTrue();
        assertThat(response.products()).isEmpty();
    }

    @Test
    void createRejectsDuplicateNameIgnoringCase() {
        when(supplierRepository.existsByNameIgnoreCase("arca continental")).thenReturn(true);

        assertThatThrownBy(() -> supplierService.create(new SupplierRequest(
                "arca continental", null, null, null, null, null, null, List.of()
        ))).isInstanceOf(BusinessException.class).hasMessage("Supplier name is already in use");

        verify(supplierRepository, never()).save(any(Supplier.class));
    }

    @Test
    void updateKeepsCurrentNameWithoutDuplicateLookup() {
        Supplier supplier = new Supplier();
        supplier.setId(5L);
        supplier.setName("Arca Continental");
        supplier.setActive(true);
        when(supplierRepository.findWithProductsById(5L)).thenReturn(Optional.of(supplier));

        var response = supplierService.update(5L, new SupplierRequest(
                " arca continental ", null, null, SupplierType.DISTRIBUTOR, null, null, null, List.of()
        ));

        assertThat(response.name()).isEqualTo("arca continental");
        verify(supplierRepository, never()).existsByNameIgnoreCase(any());
    }

    @Test
    void createPersistsSelectedProducts() {
        Product product = new Product();
        product.setId(9L);
        product.setName("Hot Dog San Fernando x3");
        when(supplierRepository.existsByNameIgnoreCase("San Fernando")).thenReturn(false);
        when(productRepository.findAllById(List.of(9L))).thenReturn(List.of(product));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier supplier = invocation.getArgument(0);
            supplier.setId(12L);
            return supplier;
        });

        var response = supplierService.create(new SupplierRequest(
                "San Fernando", null, null, SupplierType.DISTRIBUTOR, null, null, null, List.of(9L)
        ));

        assertThat(response.products()).containsExactly(
                new pe.edu.utp.ebenezer.api.dto.supplier.SupplierProductResponse(9L, "Hot Dog San Fernando x3"));
    }

    @Test
    void createRejectsMoreThanEightProducts() {
        when(supplierRepository.existsByNameIgnoreCase("San Fernando")).thenReturn(false);

        assertThatThrownBy(() -> supplierService.create(new SupplierRequest(
                "San Fernando", null, null, null, null, null, null, List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L)
        ))).isInstanceOf(BusinessException.class).hasMessage("A supplier can have at most 8 products");

        verify(supplierRepository, never()).save(any(Supplier.class));
    }

    @Test
    void findAllFiltersInTheDatabaseWithNormalizedSearch() {
        Supplier supplier = new Supplier();
        supplier.setId(3L);
        supplier.setName("Arca Continental");
        supplier.setActive(true);
        when(supplierRepository.search("%arca%", true)).thenReturn(List.of(supplier));

        var response = supplierService.findAll("  ARCA ", true);

        assertThat(response).extracting("name").containsExactly("Arca Continental");
    }

    @Test
    void findAllWithoutSearchSendsEmptyPattern() {
        when(supplierRepository.search("", null)).thenReturn(List.of());

        assertThat(supplierService.findAll("   ", null)).isEmpty();
    }
}
