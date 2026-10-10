package pe.edu.utp.ebenezer.domain.repository.inventory;

import java.math.BigDecimal;

// Current stock of a product: signed sum of its inventory movements.
public interface ProductStockView {

    Long getProductId();

    BigDecimal getStock();
}
