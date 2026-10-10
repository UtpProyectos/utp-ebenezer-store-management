package pe.edu.utp.ebenezer.service.supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.utp.ebenezer.api.dto.supplier.SupplierRequest;
import pe.edu.utp.ebenezer.domain.entity.Supplier;
import pe.edu.utp.ebenezer.domain.enums.SupplierType;
import pe.edu.utp.ebenezer.domain.repository.supplier.SupplierRepository;
import pe.edu.utp.ebenezer.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class SupplierServiceImplTest {

    @Mock
    private SupplierRepository supplierRepository;

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
                " Entrega lunes y jueves "
        ));

        assertThat(response.name()).isEqualTo("Arca Continental");
        assertThat(response.phone()).isEqualTo("981 234 567");
        assertThat(response.contactName()).isEqualTo("Carlos Mendoza");
        assertThat(response.notes()).isEqualTo("Entrega lunes y jueves");
        assertThat(response.active()).isTrue();
    }

    @Test
    void createRejectsDuplicateNameIgnoringCase() {
        when(supplierRepository.existsByNameIgnoreCase("arca continental")).thenReturn(true);

        assertThatThrownBy(() -> supplierService.create(new SupplierRequest(
                "arca continental", null, null, null, null, null, null
        ))).isInstanceOf(BusinessException.class).hasMessage("Supplier name is already in use");

        verify(supplierRepository, never()).save(any(Supplier.class));
    }

    @Test
    void updateKeepsCurrentNameWithoutDuplicateLookup() {
        Supplier supplier = new Supplier();
        supplier.setId(5L);
        supplier.setName("Arca Continental");
        supplier.setActive(true);
        when(supplierRepository.findById(5L)).thenReturn(Optional.of(supplier));

        var response = supplierService.update(5L, new SupplierRequest(
                " arca continental ", null, null, SupplierType.DISTRIBUTOR, null, null, null
        ));

        assertThat(response.name()).isEqualTo("arca continental");
        verify(supplierRepository, never()).existsByNameIgnoreCase(any());
    }
}
