package pe.edu.utp.ebenezer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.service.product.DemoCatalogSeedService;
import pe.edu.utp.ebenezer.service.supplier.DemoSupplierSeedService;
import pe.edu.utp.ebenezer.service.user.DemoFamilyUsersSeedService;

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(DemoCatalogProperties.class)
@Order(2)
public class DemoCatalogInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoCatalogInitializer.class);

    private final DemoCatalogProperties properties;
    private final DemoCatalogSeedService demoCatalogSeedService;
    private final DemoSupplierSeedService demoSupplierSeedService;
    private final DemoFamilyUsersSeedService demoFamilyUsersSeedService;

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.enabled()) {
            return;
        }
        if (demoCatalogSeedService.seedMissingDemoProducts()) {
            log.info("Missing demo catalog products added");
        } else {
            log.info("Demo catalog already contains all configured products");
        }
        if (demoSupplierSeedService.seedMissingSuppliersAndProducts()) {
            log.info("Missing demo suppliers and product associations added");
        } else {
            log.info("Demo suppliers and product associations already exist");
        }
        int createdUsers = demoFamilyUsersSeedService.seedMissingFamilyUsers();
        log.info("Created {} missing demo family users", createdUsers);
    }
}
