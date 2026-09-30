package pe.edu.utp.ebenezer.domain.repository.settings;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utp.ebenezer.domain.entity.BusinessSettings;

public interface BusinessSettingsRepository extends JpaRepository<BusinessSettings, Long> {
}
