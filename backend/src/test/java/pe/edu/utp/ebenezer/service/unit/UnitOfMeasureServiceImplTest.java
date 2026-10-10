package pe.edu.utp.ebenezer.service.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.utp.ebenezer.domain.entity.UnitOfMeasure;
import pe.edu.utp.ebenezer.domain.repository.unit.UnitOfMeasureRepository;

@ExtendWith(MockitoExtension.class)
class UnitOfMeasureServiceImplTest {

    @Mock
    private UnitOfMeasureRepository unitOfMeasureRepository;

    @InjectMocks
    private UnitOfMeasureServiceImpl unitOfMeasureService;

    @Test
    void ensureDefaultUnitsCreatesAllUnitsWhenNoneExist() {
        when(unitOfMeasureRepository.findByAbbreviation(anyString())).thenReturn(Optional.empty());

        unitOfMeasureService.ensureDefaultUnits();

        ArgumentCaptor<UnitOfMeasure> captor = ArgumentCaptor.forClass(UnitOfMeasure.class);
        verify(unitOfMeasureRepository, times(5)).save(captor.capture());
        List<String> abbreviations = captor.getAllValues().stream().map(UnitOfMeasure::getAbbreviation).toList();
        assertThat(abbreviations).containsExactly("KG", "G", "L", "ML", "UND");
    }

    @Test
    void ensureDefaultUnitsSkipsExistingUnits() {
        when(unitOfMeasureRepository.findByAbbreviation(anyString())).thenReturn(Optional.empty());
        when(unitOfMeasureRepository.findByAbbreviation("UND")).thenReturn(Optional.of(new UnitOfMeasure()));

        unitOfMeasureService.ensureDefaultUnits();

        verify(unitOfMeasureRepository, times(4)).save(any(UnitOfMeasure.class));
    }
}
