package pe.edu.utp.ebenezer.service.inventory;

import java.math.BigDecimal;

import pe.edu.utp.ebenezer.domain.entity.Lot;

/** Positive base quantity taken from a lot; {@code lot} is null for stock that entered without a lot. */
public record StockAllocation(Lot lot, BigDecimal quantity) {
}
