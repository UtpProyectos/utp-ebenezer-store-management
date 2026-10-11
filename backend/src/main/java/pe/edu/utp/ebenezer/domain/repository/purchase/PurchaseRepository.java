package pe.edu.utp.ebenezer.domain.repository.purchase;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utp.ebenezer.domain.entity.Purchase;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    boolean existsByNotes(String notes);

    // Purchases of a period with everything the history shows, in one query.
    @EntityGraph(attributePaths = {"supplier", "user", "details", "details.product", "details.unitOfMeasure"})
    List<Purchase> findByPurchaseDateGreaterThanEqualAndPurchaseDateLessThanOrderByPurchaseDateDesc(
            LocalDateTime from, LocalDateTime to);
}
