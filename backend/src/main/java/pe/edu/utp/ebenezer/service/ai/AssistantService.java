package pe.edu.utp.ebenezer.service.ai;

import pe.edu.utp.ebenezer.api.dto.ai.ChatRequest;
import pe.edu.utp.ebenezer.api.dto.ai.ChatResponse;

public interface AssistantService {

    /**
     * Answers a question about the store with the read-only tools allowed for the current user.
     *
     * @throws pe.edu.utp.ebenezer.exception.AiUnavailableException when no AI provider can answer
     */
    ChatResponse chat(ChatRequest request);
}
