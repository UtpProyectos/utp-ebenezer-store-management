package pe.edu.utp.ebenezer.api.controller.consumption;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.api.dto.consumption.InternalConsumptionRequest;
import pe.edu.utp.ebenezer.api.dto.consumption.InternalConsumptionResponse;
import pe.edu.utp.ebenezer.service.consumption.InternalConsumptionService;

// Any authenticated user (ADMIN or CASHIER) registers a consumption; the history is admin only.
@RestController
@RequestMapping("/api/internal-consumptions")
@RequiredArgsConstructor
public class InternalConsumptionController {

    private final InternalConsumptionService internalConsumptionService;

    /** Consumptions of a period (from / to, both included; today by default). */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<InternalConsumptionResponse> findByPeriod(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        LocalDate start = from != null ? from : LocalDate.now();
        return internalConsumptionService.findByPeriod(start, to != null ? to : start);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InternalConsumptionResponse create(@Valid @RequestBody InternalConsumptionRequest request) {
        return internalConsumptionService.create(request);
    }
}
