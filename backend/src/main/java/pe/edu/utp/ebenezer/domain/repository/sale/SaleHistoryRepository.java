package pe.edu.utp.ebenezer.domain.repository.sale;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utp.ebenezer.domain.entity.SaleHistory;
import pe.edu.utp.ebenezer.domain.enums.SaleHistoryAction;

public interface SaleHistoryRepository extends JpaRepository<SaleHistory, Long> {

    // Audit entries of a period, newest first, without the given action (e.g. CREATED).
    @EntityGraph(attributePaths = {"user", "sale"})
    List<SaleHistory> findByCreatedAtGreaterThanEqualAndCreatedAtLessThanAndActionNotOrderByCreatedAtDesc(
            LocalDateTime from, LocalDateTime to, SaleHistoryAction excludedAction);
}
