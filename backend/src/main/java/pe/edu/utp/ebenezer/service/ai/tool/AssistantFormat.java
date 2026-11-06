package pe.edu.utp.ebenezer.service.ai.tool;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import pe.edu.utp.ebenezer.api.dto.ai.AssistantTone;
import pe.edu.utp.ebenezer.api.dto.inventory.StockStatus;

/** Display formats for block texts (same conventions as the frontend). */
public final class AssistantFormat {

    private static final DateTimeFormatter DAY =
            DateTimeFormatter.ofPattern("EEE dd/MM", Locale.forLanguageTag("es-PE"));

    private AssistantFormat() {
    }

    public static String money(BigDecimal amount) {
        BigDecimal value = amount == null ? BigDecimal.ZERO : amount;
        return "S/ " + value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public static String quantity(BigDecimal quantity, String unit) {
        BigDecimal value = quantity == null ? BigDecimal.ZERO : quantity.setScale(3, RoundingMode.HALF_UP);
        String number = value.signum() == 0 ? "0" : value.stripTrailingZeros().toPlainString();
        return unit == null || unit.isBlank() ? number : number + " " + unit;
    }

    public static String day(LocalDate date) {
        return date.format(DAY);
    }

    public static BigDecimal round(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }

    public static String statusLabel(StockStatus status) {
        return switch (status) {
            case OK -> "Normal";
            case LOW -> "Bajo";
            case CRITICAL -> "Crítico";
            case EXPIRING_SOON -> "Por vencer";
            case EXPIRED -> "Vencido";
        };
    }

    public static AssistantTone statusTone(StockStatus status) {
        return switch (status) {
            case OK -> AssistantTone.SUCCESS;
            case LOW -> AssistantTone.WARNING;
            case CRITICAL, EXPIRED -> AssistantTone.DANGER;
            case EXPIRING_SOON -> AssistantTone.INFO;
        };
    }

    /** Bar length between 0 and 1. */
    public static Double ratio(BigDecimal value, BigDecimal max) {
        if (value == null || max == null || max.signum() <= 0) {
            return 0.0;
        }
        return Math.min(1.0, value.doubleValue() / max.doubleValue());
    }
}
