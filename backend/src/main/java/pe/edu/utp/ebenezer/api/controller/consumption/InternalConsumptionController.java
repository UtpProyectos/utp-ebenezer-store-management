package pe.edu.utp.ebenezer.api.controller.consumption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.api.dto.consumption.InternalConsumptionRequest;
import pe.edu.utp.ebenezer.api.dto.consumption.InternalConsumptionResponse;
import pe.edu.utp.ebenezer.service.consumption.InternalConsumptionService;

// Any authenticated user (ADMIN or CASHIER). Only registering a consumption is exposed for now.
@RestController
@RequestMapping("/api/internal-consumptions")
@RequiredArgsConstructor
public class InternalConsumptionController {

    private final InternalConsumptionService internalConsumptionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InternalConsumptionResponse create(@Valid @RequestBody InternalConsumptionRequest request) {
        return internalConsumptionService.create(request);
    }
}
