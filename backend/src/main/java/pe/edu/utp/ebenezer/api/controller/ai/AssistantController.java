package pe.edu.utp.ebenezer.api.controller.ai;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.api.dto.ai.ChatRequest;
import pe.edu.utp.ebenezer.api.dto.ai.ChatResponse;
import pe.edu.utp.ebenezer.service.ai.AssistantService;

// Any authenticated user. Admin-only data is filtered per tool by role (AssistantToolRegistry).
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AssistantController {

    private final AssistantService assistantService;

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return assistantService.chat(request);
    }
}
