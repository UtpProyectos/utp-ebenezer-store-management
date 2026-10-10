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

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(DemoCatalogProperties.class)
@Order(2)
public class DemoCatalogInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoCatalogInitializer.class);

    private final DemoCatalogProperties properties;
    private final DemoCatalogSeedService demoCatalogSeedService;

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.enabled()) {
            return;
        }
        if (demoCatalogSeedService.seedIfCatalogIsEmpty()) {
            log.info("Demo catalog loaded with initial stock");
        } else {
            log.info("Demo catalog seed skipped because products already exist");
        }
    }
}
