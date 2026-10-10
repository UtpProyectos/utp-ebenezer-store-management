package pe.edu.utp.ebenezer.service.supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.utp.ebenezer.domain.entity.Product;
import pe.edu.utp.ebenezer.domain.entity.Supplier;
import pe.edu.utp.ebenezer.domain.repository.product.ProductRepository;
import pe.edu.utp.ebenezer.domain.repository.supplier.SupplierRepository;

@ExtendWith(MockitoExtension.class)
class DemoSupplierSeedServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private DemoSupplierSeedService seedService;

    @Test
    void seedCreatesMissingSuppliersWhenTableIsEmpty() {
        when(supplierRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(productRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier supplier = invocation.getArgument(0);
            supplier.setId(1L);
            return supplier;
        });

        assertThat(seedService.seedMissingSuppliersAndProducts()).isTrue();

        verify(supplierRepository, times(5)).save(any(Supplier.class));
    }

    @Test
    void seedDoesNotReplaceExistingSupplierProducts() {
        Supplier existing = new Supplier();
        existing.setId(8L);
        existing.getProducts().add(new Product());
        when(supplierRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.of(existing));

        assertThat(seedService.seedMissingSuppliersAndProducts()).isFalse();

        verify(supplierRepository, org.mockito.Mockito.never()).save(any(Supplier.class));
        verify(productRepository, org.mockito.Mockito.never()).findByNameIgnoreCase(anyString());
    }

    @Test
    void associatesConfiguredProductsWithTheSanFernandoSupplier() {
        when(supplierRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(productRepository.findByNameIgnoreCase(anyString())).thenAnswer(invocation -> {
            Product product = new Product();
            product.setId(21L);
            product.setName(invocation.getArgument(0));
            return Optional.of(product);
        });
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier supplier = invocation.getArgument(0);
            supplier.setId(1L);
            return supplier;
        });

        assertThat(seedService.seedMissingSuppliersAndProducts()).isTrue();

        ArgumentCaptor<Supplier> captor = ArgumentCaptor.forClass(Supplier.class);
        verify(supplierRepository, times(10)).save(captor.capture());
        Supplier sanFernando = captor.getAllValues().stream()
                .filter(supplier -> supplier.getName().equals("Distribuidora San Fernando"))
                .findFirst()
                .orElseThrow();
        assertThat(sanFernando.getProducts())
                .extracting(Product::getName)
                .containsExactlyInAnyOrder(
                        "Hot Dog San Fernando x3",
                        "Hot Dog San Fernando x6",
                        "Jamonada San Fernando",
                        "Chicharrón de prensa San Fernando",
                        "Chorizo San Fernando"
                );
    }
}
