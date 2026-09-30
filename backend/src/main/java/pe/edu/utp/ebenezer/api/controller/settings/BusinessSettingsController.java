package pe.edu.utp.ebenezer.api.controller.settings;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.service.settings.BusinessSettingsService;

// TODO(team): expose the endpoints. Only delegate to BusinessSettingsService; never call repositories here.
@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class BusinessSettingsController {

    private final BusinessSettingsService businessSettingsService;
}
