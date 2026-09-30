package pe.edu.utp.ebenezer.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "lot", indexes = {
        @Index(name = "idx_lot_product_expiration", columnList = "product_id, expiration_date")
})
public class Lot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lot_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_detail_id", nullable = false)
    private PurchaseDetail purchaseDetail;

    @Column(name = "lot_code", length = 100)
    private String lotCode;

    @Column(name = "entry_date", nullable = false)
    private LocalDateTime entryDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;
}
