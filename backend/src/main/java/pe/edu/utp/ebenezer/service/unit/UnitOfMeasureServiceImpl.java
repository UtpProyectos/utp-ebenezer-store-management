package pe.edu.utp.ebenezer.service.unit;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.api.dto.unit.UnitOfMeasureResponse;
import pe.edu.utp.ebenezer.domain.entity.UnitOfMeasure;
import pe.edu.utp.ebenezer.domain.enums.UnitType;
import pe.edu.utp.ebenezer.domain.repository.unit.UnitOfMeasureRepository;

@Service
@RequiredArgsConstructor
public class UnitOfMeasureServiceImpl implements UnitOfMeasureService {

    private static final List<DefaultUnit> DEFAULT_UNITS = List.of(
            new DefaultUnit("Kilogramo", "KG", UnitType.WEIGHT, BigDecimal.ONE),
            new DefaultUnit("Gramo", "G", UnitType.WEIGHT, new BigDecimal("0.001")),
            new DefaultUnit("Litro", "L", UnitType.VOLUME, BigDecimal.ONE),
            new DefaultUnit("Mililitro", "ML", UnitType.VOLUME, new BigDecimal("0.001")),
            new DefaultUnit("Unidad", "UND", UnitType.UNIT, BigDecimal.ONE)
    );

    private final UnitOfMeasureRepository unitOfMeasureRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UnitOfMeasureResponse> findAll() {
        return unitOfMeasureRepository.findAll(Sort.by("name")).stream()
                .map(UnitOfMeasureServiceImpl::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void ensureDefaultUnits() {
        for (DefaultUnit defaultUnit : DEFAULT_UNITS) {
            if (unitOfMeasureRepository.findByAbbreviation(defaultUnit.abbreviation()).isPresent()) {
                continue;
            }
            UnitOfMeasure unit = new UnitOfMeasure();
            unit.setName(defaultUnit.name());
            unit.setAbbreviation(defaultUnit.abbreviation());
            unit.setType(defaultUnit.type());
            unit.setConversionFactor(defaultUnit.conversionFactor());
            unitOfMeasureRepository.save(unit);
        }
    }

    private static UnitOfMeasureResponse toResponse(UnitOfMeasure unit) {
        return new UnitOfMeasureResponse(
                unit.getId(),
                unit.getName(),
                unit.getAbbreviation(),
                unit.getType(),
                unit.getConversionFactor()
        );
    }

    private record DefaultUnit(String name, String abbreviation, UnitType type, BigDecimal conversionFactor) {
    }
}
