package pe.edu.utp.ebenezer.api.controller.unit;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.service.unit.UnitOfMeasureService;

// TODO(team): expose the endpoints. Only delegate to UnitOfMeasureService; never call repositories here.
@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
public class UnitOfMeasureController {

    private final UnitOfMeasureService unitOfMeasureService;
}
