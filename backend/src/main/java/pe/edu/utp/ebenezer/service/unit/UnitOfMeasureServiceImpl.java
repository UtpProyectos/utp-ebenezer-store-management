package pe.edu.utp.ebenezer.service.unit;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.api.dto.unit.UnitOfMeasureResponse;
import pe.edu.utp.ebenezer.domain.entity.UnitOfMeasure;
import pe.edu.utp.ebenezer.domain.repository.unit.UnitOfMeasureRepository;

@Service
@RequiredArgsConstructor
public class UnitOfMeasureServiceImpl implements UnitOfMeasureService {

    private final UnitOfMeasureRepository unitOfMeasureRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UnitOfMeasureResponse> findAll() {
        return unitOfMeasureRepository.findAll(Sort.by("name")).stream()
                .map(UnitOfMeasureServiceImpl::toResponse)
                .toList();
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
}
