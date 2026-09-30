package pe.edu.utp.ebenezer.service.settings;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.domain.repository.settings.BusinessSettingsRepository;

@Service
@RequiredArgsConstructor
public class BusinessSettingsServiceImpl implements BusinessSettingsService {

    private final BusinessSettingsRepository businessSettingsRepository;
}
