package pe.edu.utp.ebenezer.api.dto.unit;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import pe.edu.utp.ebenezer.domain.enums.UnitType;

public record UnitOfMeasureRequest(
        @NotBlank @Size(max = 50) String name,
        @NotBlank @Size(max = 10) String abbreviation,
        @NotNull UnitType type,
        @NotNull @Positive @Digits(integer = 9, fraction = 6) BigDecimal conversionFactor
) {
}
