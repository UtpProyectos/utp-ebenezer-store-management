package pe.edu.utp.ebenezer.api.controller.consumption;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.service.consumption.InternalConsumptionService;

// TODO(team): expose the endpoints. Only delegate to InternalConsumptionService; never call repositories here.
@RestController
@RequestMapping("/api/internal-consumptions")
@RequiredArgsConstructor
public class InternalConsumptionController {

    private final InternalConsumptionService internalConsumptionService;
}
