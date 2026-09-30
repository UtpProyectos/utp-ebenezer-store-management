package pe.edu.utp.ebenezer.domain.repository.purchase;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utp.ebenezer.domain.entity.PurchaseDetail;

public interface PurchaseDetailRepository extends JpaRepository<PurchaseDetail, Long> {
}
