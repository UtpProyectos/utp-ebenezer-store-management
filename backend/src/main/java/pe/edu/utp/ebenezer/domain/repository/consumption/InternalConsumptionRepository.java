package pe.edu.utp.ebenezer.domain.repository.consumption;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utp.ebenezer.domain.entity.InternalConsumption;

public interface InternalConsumptionRepository extends JpaRepository<InternalConsumption, Long> {
}
