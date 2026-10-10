package pe.edu.utp.ebenezer.domain.repository.product;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pe.edu.utp.ebenezer.domain.entity.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    boolean existsByProduct_Id(Long productId);

    // Active promotions of active products valid at the given moment (null dates = no limit), newest first.
    @Query("""
            select p from Promotion p
            join fetch p.product pr
            join fetch pr.baseUnit
            join fetch p.unitOfMeasure
            where p.active = true
              and pr.active = true
              and (p.startDate is null or p.startDate <= :now)
              and (p.endDate is null or p.endDate >= :now)
            order by p.id desc""")
    List<Promotion> findCurrent(@Param("now") LocalDateTime now);
}
