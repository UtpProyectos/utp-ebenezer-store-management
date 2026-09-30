package pe.edu.utp.ebenezer.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;

import pe.edu.utp.ebenezer.domain.enums.ShoppingListSource;
import pe.edu.utp.ebenezer.domain.enums.ShoppingListStatus;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "shopping_list")
public class ShoppingList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shopping_list_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 30)
    private ShoppingListSource source;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ShoppingListStatus status;

    @Column(name = "estimated_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal estimatedCost = BigDecimal.ZERO;

    // Header -> detail only. No CascadeType.REMOVE: records are never physically deleted in cascade.
    @OneToMany(mappedBy = "shoppingList", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<ShoppingListDetail> details = new ArrayList<>();
}
