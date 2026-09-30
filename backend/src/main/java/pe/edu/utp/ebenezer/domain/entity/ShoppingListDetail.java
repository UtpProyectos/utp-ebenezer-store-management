package pe.edu.utp.ebenezer.domain.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "shopping_list_detail")
public class ShoppingListDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shopping_list_detail_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shopping_list_id", nullable = false)
    private ShoppingList shoppingList;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unit_of_measure_id", nullable = false)
    private UnitOfMeasure unitOfMeasure;

    @Column(name = "suggested_quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal suggestedQuantity;

    @Column(name = "purchased_quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal purchasedQuantity = BigDecimal.ZERO;

    @Column(name = "estimated_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal estimatedCost = BigDecimal.ZERO;

    @Column(name = "purchased", nullable = false)
    private Boolean purchased = false;
}
