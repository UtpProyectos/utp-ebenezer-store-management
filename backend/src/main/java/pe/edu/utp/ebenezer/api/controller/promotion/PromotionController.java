package pe.edu.utp.ebenezer.api.controller.promotion;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.service.promotion.PromotionService;

// TODO(team): expose the endpoints. Only delegate to PromotionService; never call repositories here.
@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;
}
