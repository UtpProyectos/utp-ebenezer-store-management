package pe.edu.utp.ebenezer.api.dto.inventory;

// Derived from stock and lot expiration; never persisted.
public enum StockStatus {
    OK,
    LOW,
    CRITICAL,
    EXPIRING_SOON,
    EXPIRED
}
