package pe.edu.utp.ebenezer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.demo-catalog")
public record DemoCatalogProperties(boolean enabled) {
}
