package pe.edu.utp.ebenezer.service.ai.tool;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

import tools.jackson.databind.JsonNode;

/** Reads and validates tool arguments. */
public final class ToolArguments {

    /** Longest period a tool may read, to keep queries and token usage small. */
    public static final int MAX_PERIOD_DAYS = 93;

    private ToolArguments() {
    }

    public record DateRange(LocalDate from, LocalDate to) {

        public long days() {
            return ChronoUnit.DAYS.between(from, to) + 1;
        }
    }

    public static String text(JsonNode arguments, String field) {
        JsonNode value = arguments.path(field);
        if (!value.isString()) {
            return null;
        }
        String text = value.asString().trim();
        return text.isEmpty() ? null : text;
    }

    public static int integer(JsonNode arguments, String field, int defaultValue, int min, int max) {
        JsonNode value = arguments.path(field);
        if (!value.isNumber()) {
            return defaultValue;
        }
        return Math.clamp(value.asInt(), min, max);
    }

    /** Period from the "from" / "to" arguments (yyyy-MM-dd); missing values use the defaults. */
    public static DateRange dateRange(JsonNode arguments, LocalDate defaultFrom, LocalDate defaultTo) {
        LocalDate to = date(arguments, "to", defaultTo);
        LocalDate from = date(arguments, "from", defaultFrom.isAfter(to) ? to : defaultFrom);
        if (from.isAfter(to)) {
            throw new InvalidToolArgumentException("La fecha inicial es posterior a la final.");
        }
        DateRange range = new DateRange(from, to);
        if (range.days() > MAX_PERIOD_DAYS) {
            throw new InvalidToolArgumentException(
                    "El período es muy largo: el máximo es " + MAX_PERIOD_DAYS + " días.");
        }
        return range;
    }

    private static LocalDate date(JsonNode arguments, String field, LocalDate defaultValue) {
        String text = text(arguments, field);
        if (text == null) {
            return defaultValue;
        }
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException ex) {
            throw new InvalidToolArgumentException("Fecha inválida en " + field + ": usa el formato yyyy-MM-dd.");
        }
    }
}
