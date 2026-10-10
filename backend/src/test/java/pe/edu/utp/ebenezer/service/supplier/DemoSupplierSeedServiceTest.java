package pe.edu.utp.ebenezer.service.supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.utp.ebenezer.domain.entity.Supplier;
import pe.edu.utp.ebenezer.domain.repository.supplier.SupplierRepository;

@ExtendWith(MockitoExtension.class)
class DemoSupplierSeedServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private DemoSupplierSeedService seedService;

    @Test
    void seedCreatesFiveSuppliersWhenTableIsEmpty() {
        when(supplierRepository.count()).thenReturn(0L);

        assertThat(seedService.seedIfSuppliersAreEmpty()).isTrue();

        verify(supplierRepository).saveAll(argThat(suppliers -> {
            List<Supplier> saved = new ArrayList<>();
            suppliers.forEach(saved::add);
            return saved.size() == 5;
        }));
    }

    @Test
    void seedSkipsWhenSuppliersAlreadyExist() {
        when(supplierRepository.count()).thenReturn(2L);

        assertThat(seedService.seedIfSuppliersAreEmpty()).isFalse();

        verify(supplierRepository, never()).saveAll(anyList());
    }
}
