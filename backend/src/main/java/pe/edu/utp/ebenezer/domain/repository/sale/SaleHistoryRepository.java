package pe.edu.utp.ebenezer.domain.repository.sale;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utp.ebenezer.domain.entity.SaleHistory;

public interface SaleHistoryRepository extends JpaRepository<SaleHistory, Long> {
}
