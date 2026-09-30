package pe.edu.utp.ebenezer.service.purchase;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.domain.repository.inventory.InventoryMovementRepository;
import pe.edu.utp.ebenezer.domain.repository.inventory.LotRepository;
import pe.edu.utp.ebenezer.domain.repository.purchase.PurchaseDetailRepository;
import pe.edu.utp.ebenezer.domain.repository.purchase.PurchaseRepository;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseDetailRepository purchaseDetailRepository;
    private final LotRepository lotRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
}
