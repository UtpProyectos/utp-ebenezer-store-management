package pe.edu.utp.ebenezer.domain.repository.sale;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utp.ebenezer.domain.entity.Sale;

public interface SaleRepository extends JpaRepository<Sale, Long> {
}
