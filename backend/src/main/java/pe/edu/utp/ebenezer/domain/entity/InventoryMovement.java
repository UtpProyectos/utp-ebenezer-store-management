package pe.edu.utp.ebenezer.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import pe.edu.utp.ebenezer.domain.enums.InventoryMovementType;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "inventory_movement", indexes = {
        @Index(name = "idx_inventory_movement_product_date", columnList = "product_id, movement_date"),
        @Index(name = "idx_inventory_movement_lot_date", columnList = "lot_id, movement_date"),
        @Index(name = "idx_inventory_movement_type", columnList = "movement_type")
})
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_movement_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id")
    private Lot lot;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 40)
    private InventoryMovementType movementType;

    @Column(name = "base_quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal baseQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_detail_id")
    private PurchaseDetail purchaseDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_detail_id")
    private SaleDetail saleDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internal_consumption_detail_id")
    private InternalConsumptionDetail internalConsumptionDetail;

    @Column(name = "movement_date", nullable = false)
    private LocalDateTime movementDate;

    @Column(name = "reason", length = 150)
    private String reason;

    @Column(name = "notes", length = 500)
    private String notes;
}
