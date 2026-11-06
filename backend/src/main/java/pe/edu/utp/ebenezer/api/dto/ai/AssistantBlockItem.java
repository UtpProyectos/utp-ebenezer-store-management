package pe.edu.utp.ebenezer.api.dto.ai;

/**
 * One row, tile or bar of a block. Texts are already formatted for display (Spanish, S/ amounts).
 *
 * @param label main text (product, supplier, day, figure name)
 * @param value secondary text (quantity, amount)
 * @param badge short status text for LIST rows; null otherwise
 * @param tone  color of the badge or bar
 * @param ratio bar length between 0 and 1 for BARS; null otherwise
 */
public record AssistantBlockItem(String label, String value, String badge, AssistantTone tone, Double ratio) {
}
