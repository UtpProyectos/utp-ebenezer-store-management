package pe.edu.utp.ebenezer.domain.repository.purchase;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utp.ebenezer.domain.entity.Purchase;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    boolean existsByNotes(String notes);
}
