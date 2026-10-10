package pe.edu.utp.ebenezer.api.controller.sale;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.api.dto.sale.SaleCancelRequest;
import pe.edu.utp.ebenezer.api.dto.sale.SaleHistoryResponse;
import pe.edu.utp.ebenezer.api.dto.sale.SaleRequest;
import pe.edu.utp.ebenezer.api.dto.sale.SaleResponse;
import pe.edu.utp.ebenezer.api.dto.sale.SaleUpdateRequest;
import pe.edu.utp.ebenezer.service.sale.SaleService;

// Any authenticated user (ADMIN or CASHIER), as in the prototype: every edit and cancellation stays audited.
@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    /** Sales of a day (today when no date is given). */
    @GetMapping
    public List<SaleResponse> findByDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return saleService.findByDate(date != null ? date : LocalDate.now());
    }

    /** Edits and cancellations made on a day (today when no date is given). */
    @GetMapping("/changes")
    public List<SaleHistoryResponse> findChanges(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return saleService.findChanges(date != null ? date : LocalDate.now());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SaleResponse create(@Valid @RequestBody SaleRequest request) {
        return saleService.create(request);
    }

    @PutMapping("/{id}")
    public SaleResponse update(@PathVariable Long id, @Valid @RequestBody SaleUpdateRequest request) {
        return saleService.update(id, request);
    }

    @PatchMapping("/{id}/cancel")
    public SaleResponse cancel(@PathVariable Long id, @Valid @RequestBody SaleCancelRequest request) {
        return saleService.cancel(id, request);
    }
}
