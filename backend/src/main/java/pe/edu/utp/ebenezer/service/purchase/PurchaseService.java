package pe.edu.utp.ebenezer.service.purchase;

import java.time.LocalDate;
import java.util.List;

import pe.edu.utp.ebenezer.api.dto.purchase.PurchaseRequest;
import pe.edu.utp.ebenezer.api.dto.purchase.PurchaseResponse;

public interface PurchaseService {

    /** Purchases registered between two days (both included), newest first. */
    List<PurchaseResponse> findByPeriod(LocalDate from, LocalDate to);

    /** Registers a purchase: details, one lot per detail and a positive PURCHASE movement per lot. */
    PurchaseResponse create(PurchaseRequest request);
}
