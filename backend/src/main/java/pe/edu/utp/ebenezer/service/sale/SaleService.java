package pe.edu.utp.ebenezer.service.sale;

import java.time.LocalDate;
import java.util.List;

import pe.edu.utp.ebenezer.api.dto.sale.SaleCancelRequest;
import pe.edu.utp.ebenezer.api.dto.sale.SaleHistoryResponse;
import pe.edu.utp.ebenezer.api.dto.sale.SaleRequest;
import pe.edu.utp.ebenezer.api.dto.sale.SaleResponse;
import pe.edu.utp.ebenezer.api.dto.sale.SaleUpdateRequest;

public interface SaleService {

    /**
     * Registers a confirmed sale: prices from the product (and its current promotion), FEFO over
     * non-expired stock, one negative SALE movement per lot, and a CREATED history entry.
     */
    SaleResponse create(SaleRequest request);

    /** Sales of a day (every status), newest first. */
    List<SaleResponse> findByDate(LocalDate date);

    /** Edits and cancellations made on a day, newest first (CREATED entries are left out). */
    List<SaleHistoryResponse> findChanges(LocalDate date);

    /**
     * Corrects payment method and line quantities. Changed lines are reversed (REVERSAL) and allocated
     * again with FEFO; the sale becomes EDITED and an EDITED history entry keeps both versions.
     */
    SaleResponse update(Long id, SaleUpdateRequest request);

    /** Cancels a sale: never deleted, its stock comes back through REVERSAL movements. */
    SaleResponse cancel(Long id, SaleCancelRequest request);
}
