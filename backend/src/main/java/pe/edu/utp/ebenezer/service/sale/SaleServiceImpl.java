package pe.edu.utp.ebenezer.service.sale;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.domain.repository.inventory.InventoryMovementRepository;
import pe.edu.utp.ebenezer.domain.repository.sale.SaleDetailRepository;
import pe.edu.utp.ebenezer.domain.repository.sale.SaleHistoryRepository;
import pe.edu.utp.ebenezer.domain.repository.sale.SaleRepository;

@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final SaleDetailRepository saleDetailRepository;
    private final SaleHistoryRepository saleHistoryRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
}
