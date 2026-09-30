package pe.edu.utp.ebenezer.domain.repository.inventory;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utp.ebenezer.domain.entity.Lot;

public interface LotRepository extends JpaRepository<Lot, Long> {
}
