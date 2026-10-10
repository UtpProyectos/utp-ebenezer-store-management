package pe.edu.utp.ebenezer.api.controller.unit;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.api.dto.unit.UnitOfMeasureResponse;
import pe.edu.utp.ebenezer.service.unit.UnitOfMeasureService;

@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
public class UnitOfMeasureController {

    private final UnitOfMeasureService unitOfMeasureService;

    @GetMapping
    public List<UnitOfMeasureResponse> findAll() {
        return unitOfMeasureService.findAll();
    }
}
