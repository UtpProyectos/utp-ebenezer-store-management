package pe.edu.utp.ebenezer.api.dto.ai;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * A question for the assistant. The conversation is not stored on the server, so the client sends
 * the latest turns as history (oldest first).
 */
public record ChatRequest(
        @NotBlank @Size(max = 500) String message,
        @Size(max = 10) List<@Valid ChatHistoryMessage> history
) {
}
