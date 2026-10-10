package pe.edu.utp.ebenezer.domain.repository.inventory;

import java.math.BigDecimal;
import java.time.LocalDate;

// Current stock of a lot: signed sum of its inventory movements.
public interface LotStockView {

    Long getLotId();

    Long getProductId();

    LocalDate getExpirationDate();

    BigDecimal getStock();
}
