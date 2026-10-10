package pe.edu.utp.ebenezer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.service.user.RoleService;
import pe.edu.utp.ebenezer.service.user.UserService;

/**
 * Seeds the roles and the initial admin on startup. Only delegates to services.
 */
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(BootstrapAdminProperties.class)
@Order(1)
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleService roleService;
    private final UserService userService;
    private final BootstrapAdminProperties adminProperties;

    @Override
    public void run(ApplicationArguments args) {
        roleService.ensureDefaultRoles();

        if (isBlank(adminProperties.username()) || isBlank(adminProperties.password())) {
            log.warn("ADMIN_USERNAME/ADMIN_PASSWORD not configured: initial admin user will not be created");
            return;
        }
        String name = isBlank(adminProperties.name()) ? adminProperties.username() : adminProperties.name();
        if (userService.createInitialAdminIfMissing(name.trim(), adminProperties.username().trim(),
                adminProperties.password())) {
            log.info("Initial admin user '{}' created", adminProperties.username().trim());
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
