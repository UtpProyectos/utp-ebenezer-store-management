package pe.edu.utp.ebenezer.service.sale;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import pe.edu.utp.ebenezer.domain.entity.Promotion;
import pe.edu.utp.ebenezer.domain.entity.UnitOfMeasure;
import pe.edu.utp.ebenezer.domain.enums.UnitType;

class SalePricingTest {

    private static final UnitOfMeasure UND = unit("UND", UnitType.UNIT, "1");
    private static final UnitOfMeasure KG = unit("KG", UnitType.WEIGHT, "1");
    private static final UnitOfMeasure G = unit("G", UnitType.WEIGHT, "0.001");

    @Test
    void withoutPromotionTheLineIsQuantityTimesPrice() {
        SalePricing.LinePrice price = SalePricing.price(new BigDecimal("3"), new BigDecimal("2.50"), null, UND);

        assertThat(price.gross()).isEqualByComparingTo("7.50");
        assertThat(price.promotionDiscount()).isEqualByComparingTo("0");
    }

    @Test
    void repeatablePromotionAppliesToEveryCompleteGroup() {
        // 3 x S/ 2.00, regular S/ 0.80: 7 units = 2 groups (4.00) + 1 unit (0.80) = 4.80.
        SalePricing.LinePrice price = SalePricing.price(
                new BigDecimal("7"), new BigDecimal("0.80"), promotion(UND, "3", "2.00", true), UND);

        assertThat(price.gross()).isEqualByComparingTo("5.60");
        assertThat(price.promotionDiscount()).isEqualByComparingTo("0.80");
    }

    @Test
    void nonRepeatablePromotionAppliesOnce() {
        SalePricing.LinePrice price = SalePricing.price(
                new BigDecimal("7"), new BigDecimal("0.80"), promotion(UND, "3", "2.00", false), UND);

        assertThat(price.promotionDiscount()).isEqualByComparingTo("0.40");
    }

    @Test
    void promotionInGramsAppliesToProductSoldByKilogram() {
        // 500 G x S/ 2.00, regular S/ 4.20 per KG: 1.250 KG = 2 groups (4.00) + 0.250 KG (1.05) = 5.05.
        SalePricing.LinePrice price = SalePricing.price(
                new BigDecimal("1.250"), new BigDecimal("4.20"), promotion(G, "500", "2.00", true), KG);

        assertThat(price.gross()).isEqualByComparingTo("5.25");
        assertThat(price.promotionDiscount()).isEqualByComparingTo("0.20");
    }

    @Test
    void quantityBelowThePromotionGetsNoDiscount() {
        SalePricing.LinePrice price = SalePricing.price(
                new BigDecimal("2"), new BigDecimal("0.80"), promotion(UND, "3", "2.00", true), UND);

        assertThat(price.promotionDiscount()).isEqualByComparingTo("0");
    }

    @Test
    void promotionMoreExpensiveThanRegularPriceIsIgnored() {
        SalePricing.LinePrice price = SalePricing.price(
                new BigDecimal("3"), new BigDecimal("0.50"), promotion(UND, "3", "2.00", true), UND);

        assertThat(price.promotionDiscount()).isEqualByComparingTo("0");
    }

    private static Promotion promotion(UnitOfMeasure unit, String quantity, String price, boolean repeatable) {
        Promotion promotion = new Promotion();
        promotion.setUnitOfMeasure(unit);
        promotion.setPromotionQuantity(new BigDecimal(quantity));
        promotion.setPromotionalPrice(new BigDecimal(price));
        promotion.setRepeatable(repeatable);
        return promotion;
    }

    private static UnitOfMeasure unit(String abbreviation, UnitType type, String factor) {
        UnitOfMeasure unit = new UnitOfMeasure();
        unit.setAbbreviation(abbreviation);
        unit.setType(type);
        unit.setConversionFactor(new BigDecimal(factor));
        return unit;
    }
}
