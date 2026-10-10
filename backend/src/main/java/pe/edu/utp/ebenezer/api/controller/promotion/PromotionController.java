package pe.edu.utp.ebenezer.api.controller.promotion;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.api.dto.promotion.PromotionResponse;
import pe.edu.utp.ebenezer.service.promotion.PromotionService;

// Any authenticated user (ADMIN or CASHIER): the sales screen shows the promotions. Only reading for now.
@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    /** Promotions that apply right now (active and within their dates). */
    @GetMapping
    public List<PromotionResponse> findCurrent() {
        return promotionService.findCurrent();
    }
}
