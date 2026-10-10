package pe.edu.utp.ebenezer.service.sale;

import java.math.BigDecimal;
import java.math.RoundingMode;

import pe.edu.utp.ebenezer.domain.entity.Promotion;
import pe.edu.utp.ebenezer.domain.entity.UnitOfMeasure;
import pe.edu.utp.ebenezer.service.unit.UnitConverter;

// Line amounts of a sale (AGENTS/database.md §5). Pure calculation: no repositories.
final class SalePricing {

    private SalePricing() {
    }

    /** Amount at the regular price and the saving given by the promotion (zero when none applies). */
    record LinePrice(BigDecimal gross, BigDecimal promotionDiscount) {
    }

    /**
     * @param baseQuantity quantity in the product base unit
     * @param salePrice    regular price per base unit
     * @param promotion    current promotion of the product, or null
     * @param baseUnit     product base unit
     */
    static LinePrice price(BigDecimal baseQuantity, BigDecimal salePrice, Promotion promotion, UnitOfMeasure baseUnit) {
        BigDecimal gross = money(baseQuantity.multiply(salePrice));
        if (promotion == null || !UnitConverter.areCompatible(promotion.getUnitOfMeasure(), baseUnit)) {
            return new LinePrice(gross, BigDecimal.ZERO);
        }
        // promotion_quantity is expressed in the promotion unit; its base quantity is derived, never stored.
        BigDecimal promotionBase = UnitConverter.convert(
                promotion.getPromotionQuantity(), promotion.getUnitOfMeasure(), baseUnit);
        if (promotionBase.signum() <= 0) {
            return new LinePrice(gross, BigDecimal.ZERO);
        }
        BigDecimal groups = baseQuantity.divideToIntegralValue(promotionBase);
        if (!Boolean.TRUE.equals(promotion.getRepeatable())) {
            groups = groups.min(BigDecimal.ONE);
        }
        if (groups.signum() == 0) {
            return new LinePrice(gross, BigDecimal.ZERO);
        }
        BigDecimal rest = baseQuantity.subtract(groups.multiply(promotionBase));
        BigDecimal promotional = money(groups.multiply(promotion.getPromotionalPrice()).add(rest.multiply(salePrice)));
        // A promotion never makes the line more expensive.
        return new LinePrice(gross, gross.subtract(promotional).max(BigDecimal.ZERO));
    }

    static BigDecimal money(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}
