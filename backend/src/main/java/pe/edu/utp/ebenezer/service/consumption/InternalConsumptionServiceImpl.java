package pe.edu.utp.ebenezer.service.consumption;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.domain.repository.consumption.InternalConsumptionDetailRepository;
import pe.edu.utp.ebenezer.domain.repository.consumption.InternalConsumptionRepository;
import pe.edu.utp.ebenezer.domain.repository.inventory.InventoryMovementRepository;

@Service
@RequiredArgsConstructor
public class InternalConsumptionServiceImpl implements InternalConsumptionService {

    private final InternalConsumptionRepository internalConsumptionRepository;
    private final InternalConsumptionDetailRepository internalConsumptionDetailRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
}
