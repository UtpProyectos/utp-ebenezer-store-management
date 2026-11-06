package pe.edu.utp.ebenezer.ai.model;

import java.time.LocalDateTime;

import pe.edu.utp.ebenezer.domain.enums.RoleName;

/**
 * Who is asking and when; injected into the system prompt.
 */
public record AssistantContext(String userName, RoleName role, LocalDateTime now) {
}
