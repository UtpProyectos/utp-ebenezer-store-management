package pe.edu.utp.ebenezer.domain.repository.consumption;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utp.ebenezer.domain.entity.InternalConsumptionDetail;

public interface InternalConsumptionDetailRepository extends JpaRepository<InternalConsumptionDetail, Long> {
}
