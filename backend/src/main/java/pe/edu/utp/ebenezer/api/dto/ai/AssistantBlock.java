package pe.edu.utp.ebenezer.api.dto.ai;

import java.util.List;

/** A group of data shown under the answer. */
public record AssistantBlock(AssistantBlockType type, String title, List<AssistantBlockItem> items) {
}
