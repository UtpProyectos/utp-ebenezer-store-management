package pe.edu.utp.ebenezer.api.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** A previous turn of the conversation, as shown in the chat. */
public record ChatHistoryMessage(
        @NotNull ChatRole role,
        @NotBlank @Size(max = 1000) String content
) {
}
