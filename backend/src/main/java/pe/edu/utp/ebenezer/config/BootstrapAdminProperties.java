package pe.edu.utp.ebenezer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Initial admin created on first startup (only when there are no users).
 * Values come from ADMIN_NAME, ADMIN_USERNAME and ADMIN_PASSWORD.
 */
@ConfigurationProperties(prefix = "app.bootstrap.admin")
public record BootstrapAdminProperties(String name, String username, String password) {
}
