package pe.edu.utp.ebenezer.service.sale;

import pe.edu.utp.ebenezer.api.dto.sale.SaleRequest;
import pe.edu.utp.ebenezer.api.dto.sale.SaleResponse;

public interface SaleService {

    /**
     * Registers a confirmed sale: prices from the product (and its current promotion), FEFO over
     * non-expired stock, one negative SALE movement per lot, and a CREATED history entry.
     */
    SaleResponse create(SaleRequest request);
}
