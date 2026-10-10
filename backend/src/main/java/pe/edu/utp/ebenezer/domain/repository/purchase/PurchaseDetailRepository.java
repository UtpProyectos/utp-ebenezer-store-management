package pe.edu.utp.ebenezer.domain.repository.purchase;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pe.edu.utp.ebenezer.domain.entity.PurchaseDetail;
import pe.edu.utp.ebenezer.domain.enums.PurchaseStatus;

public interface PurchaseDetailRepository extends JpaRepository<PurchaseDetail, Long> {

    // Most recent purchase detail of each product (with its purchase and supplier).
    @Query("""
            select d from PurchaseDetail d
            join fetch d.purchase p
            left join fetch p.supplier
            where d.id in (
                select max(d2.id) from PurchaseDetail d2
                where d2.purchase.status = :status
                group by d2.product.id)""")
    List<PurchaseDetail> findLatestByProduct(@Param("status") PurchaseStatus status);
}
