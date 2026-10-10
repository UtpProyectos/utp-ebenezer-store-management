package pe.edu.utp.ebenezer.service.purchase;

import pe.edu.utp.ebenezer.api.dto.purchase.PurchaseRequest;
import pe.edu.utp.ebenezer.api.dto.purchase.PurchaseResponse;

public interface PurchaseService {

    /** Registers a purchase: details, one lot per detail and a positive PURCHASE movement per lot. */
    PurchaseResponse create(PurchaseRequest request);
}
