package pe.edu.utp.ebenezer.service.shoppinglist;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Restock rule shared by the shopping list and the AI assistant, so both suggest the same quantities.
 * Quantities are expressed in the product base unit.
 */
public final class RestockCalculator {

    private static final BigDecimal TWO = BigDecimal.valueOf(2);

    private RestockCalculator() {
    }

    /** A product needs restocking when its stock is at or below the minimum. */
    public static boolean needsRestock(BigDecimal stock, BigDecimal minStock) {
        return stock.compareTo(minStock) <= 0;
    }

    /** Same suggestion as the prototype: enough to reach twice the minimum stock, at least one. */
    public static BigDecimal suggestedQuantity(BigDecimal minStock, BigDecimal stock) {
        BigDecimal missing = minStock.multiply(TWO).subtract(stock).setScale(0, RoundingMode.CEILING);
        return missing.max(BigDecimal.ONE);
    }
}
