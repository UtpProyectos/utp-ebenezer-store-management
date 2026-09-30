package pe.edu.utp.ebenezer.api.dto.unit;

import java.math.BigDecimal;

import pe.edu.utp.ebenezer.domain.enums.UnitType;

public record UnitOfMeasureResponse(
        Long id,
        String name,
        String abbreviation,
        UnitType type,
        BigDecimal conversionFactor
) {
}
