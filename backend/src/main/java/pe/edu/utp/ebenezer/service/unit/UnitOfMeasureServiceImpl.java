package pe.edu.utp.ebenezer.service.unit;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.domain.repository.unit.UnitOfMeasureRepository;

@Service
@RequiredArgsConstructor
public class UnitOfMeasureServiceImpl implements UnitOfMeasureService {

    private final UnitOfMeasureRepository unitOfMeasureRepository;
}
