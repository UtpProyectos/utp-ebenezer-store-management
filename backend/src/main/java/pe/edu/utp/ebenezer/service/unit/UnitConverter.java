package pe.edu.utp.ebenezer.service.unit;

import java.math.BigDecimal;
import java.math.RoundingMode;

import pe.edu.utp.ebenezer.domain.entity.UnitOfMeasure;
import pe.edu.utp.ebenezer.exception.BusinessException;

// Unit conversions belong to services (AGENTS/database.md §21): quantity × factor(unit) / factor(target unit).
public final class UnitConverter {

    private UnitConverter() {
    }

    public static boolean areCompatible(UnitOfMeasure unit, UnitOfMeasure other) {
        return unit.getType() == other.getType();
    }

    /** Converts a quantity to the product base unit. Rejects incompatible units and quantities that round to zero. */
    public static BigDecimal toBaseQuantity(BigDecimal quantity, UnitOfMeasure unit, UnitOfMeasure baseUnit) {
        if (!areCompatible(unit, baseUnit)) {
            throw new BusinessException("Unit " + unit.getAbbreviation()
                    + " is not compatible with product base unit " + baseUnit.getAbbreviation());
        }
        BigDecimal baseQuantity = convert(quantity, unit, baseUnit);
        if (baseQuantity.signum() <= 0) {
            throw new BusinessException("Quantity is too small for the product base unit");
        }
        return baseQuantity;
    }

    /** Converts a quantity between two compatible units (3 decimals, as stored). */
    public static BigDecimal convert(BigDecimal quantity, UnitOfMeasure from, UnitOfMeasure to) {
        return quantity.multiply(from.getConversionFactor())
                .divide(to.getConversionFactor(), 3, RoundingMode.HALF_UP);
    }

    /** Converts a price per {@code baseUnit} into a price per {@code unit} (4 decimals, as stored). */
    public static BigDecimal convertPrice(BigDecimal pricePerBaseUnit, UnitOfMeasure unit, UnitOfMeasure baseUnit) {
        return pricePerBaseUnit.multiply(unit.getConversionFactor())
                .divide(baseUnit.getConversionFactor(), 4, RoundingMode.HALF_UP);
    }
}
