package pe.edu.utp.ebenezer.service.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.domain.entity.Category;
import pe.edu.utp.ebenezer.domain.entity.InventoryMovement;
import pe.edu.utp.ebenezer.domain.entity.Product;
import pe.edu.utp.ebenezer.domain.entity.UnitOfMeasure;
import pe.edu.utp.ebenezer.domain.entity.User;
import pe.edu.utp.ebenezer.domain.enums.InventoryMovementType;
import pe.edu.utp.ebenezer.domain.enums.RoleName;
import pe.edu.utp.ebenezer.domain.enums.UnitType;
import pe.edu.utp.ebenezer.domain.repository.category.CategoryRepository;
import pe.edu.utp.ebenezer.domain.repository.inventory.InventoryMovementRepository;
import pe.edu.utp.ebenezer.domain.repository.product.ProductRepository;
import pe.edu.utp.ebenezer.domain.repository.unit.UnitOfMeasureRepository;
import pe.edu.utp.ebenezer.domain.repository.user.UserRepository;

@Service
@RequiredArgsConstructor
public class DemoCatalogSeedService {

    private static final String INITIAL_STOCK_REASON = "Initial demo inventory";
    private static final String SEED_VERSION = "demo catalog v1";

    private final CategoryRepository categoryRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final ProductRepository productRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final UserRepository userRepository;

    @Transactional
    public boolean seedMissingDemoProducts() {
        Map<String, Category> categories = getOrCreateCategories();
        List<SeedProduct> missingProducts = DEMO_PRODUCTS.stream()
                .filter(seed -> productRepository.findByNameIgnoreCase(seed.name()).isEmpty())
                .toList();
        if (missingProducts.isEmpty()) {
            return false;
        }

        UnitOfMeasure baseUnit = getOrCreateUnit();
        boolean needsInitialStock = missingProducts.stream().anyMatch(seed -> seed.stock() > 0);
        User admin = needsInitialStock
                ? userRepository.findFirstByRole_NameAndActiveTrueOrderByIdAsc(RoleName.ADMIN)
                        .orElseThrow(() -> new IllegalStateException(
                                "Demo catalog requires an active ADMIN user before initial stock can be loaded"))
                : null;

        List<Product> products = missingProducts.stream()
                .map(seed -> createProduct(seed, categories.get(seed.category()), baseUnit))
                .toList();
        List<Product> savedProducts = productRepository.saveAll(products);

        List<InventoryMovement> initialMovements = new ArrayList<>();
        for (int i = 0; i < savedProducts.size(); i++) {
            SeedProduct seed = missingProducts.get(i);
            if (seed.stock() == 0) {
                continue;
            }
            Product product = savedProducts.get(i);
            InventoryMovement movement = new InventoryMovement();
            movement.setProduct(product);
            movement.setUser(admin);
            movement.setMovementType(InventoryMovementType.ADJUSTMENT_IN);
            movement.setBaseQuantity(BigDecimal.valueOf(seed.stock()));
            movement.setMovementDate(LocalDateTime.now());
            movement.setReason(INITIAL_STOCK_REASON);
            movement.setNotes(SEED_VERSION);
            initialMovements.add(movement);
        }
        if (!initialMovements.isEmpty()) {
            inventoryMovementRepository.saveAll(initialMovements);
        }
        return true;
    }

    private UnitOfMeasure getOrCreateUnit() {
        return unitOfMeasureRepository.findByAbbreviation("UND")
                .map(unit -> {
                    if (unit.getType() != UnitType.UNIT
                            || unit.getConversionFactor().compareTo(BigDecimal.ONE) != 0) {
                        throw new IllegalStateException(
                                "Existing UND unit must have type UNIT and conversion factor 1");
                    }
                    return unit;
                })
                .orElseGet(() -> {
                    UnitOfMeasure unit = new UnitOfMeasure();
                    unit.setName("Unidad");
                    unit.setAbbreviation("UND");
                    unit.setType(UnitType.UNIT);
                    unit.setConversionFactor(BigDecimal.ONE);
                    return unitOfMeasureRepository.save(unit);
                });
    }

    private Map<String, Category> getOrCreateCategories() {
        Map<String, Category> categories = new LinkedHashMap<>();
        for (String name : DEMO_CATEGORIES) {
            Category category = categoryRepository.findByName(name)
                    .orElseGet(() -> {
                        Category newCategory = new Category();
                        newCategory.setName(name);
                        return categoryRepository.save(newCategory);
                    });
            if (!category.getActive()) {
                throw new IllegalStateException("Demo category is inactive: " + name);
            }
            categories.put(name, category);
        }
        return categories;
    }

    private static Product createProduct(SeedProduct seed, Category category, UnitOfMeasure baseUnit) {
        Product product = new Product();
        product.setCategory(category);
        product.setBaseUnit(baseUnit);
        product.setName(seed.name());
        product.setSalePrice(new BigDecimal(seed.price()));
        product.setMinStock(BigDecimal.ZERO);
        product.setActive(true);
        return product;
    }

    private record SeedProduct(String name, String price, int stock, String category) {
    }

    private static final List<String> DEMO_CATEGORIES = List.of(
            "Bebidas",
            "Lácteos y Derivados",
            "Abarrotes Básicos",
            "Limpieza e Higiene",
            "Snacks y Golosinas",
            "Librería y Escolares",
            "Regalos y Tecnología",
            "Embutidos"
    );

    private static final List<SeedProduct> DEMO_PRODUCTS = List.of(
            new SeedProduct("Gaseosa Inca Kola 500 ml", "2.50", 34, "Bebidas"),
            new SeedProduct("Gaseosa Coca-Cola 1.5 L", "7.50", 12, "Bebidas"),
            new SeedProduct("Agua mineral San Luis 625 ml", "1.50", 6, "Bebidas"),
            new SeedProduct("Jugo Frugos del Valle Durazno 1 L", "4.50", 15, "Bebidas"),
            new SeedProduct("Bebida rehidratante Sporade 500 ml", "2.00", 22, "Bebidas"),
            new SeedProduct("Leche evaporada Gloria Azul 400 g", "4.20", 18, "Lácteos y Derivados"),
            new SeedProduct("Yogurt de fresa Gloria 1 L", "6.50", 5, "Lácteos y Derivados"),
            new SeedProduct("Queso fresco Laive 250 g", "8.00", 3, "Lácteos y Derivados"),
            new SeedProduct("Mantequilla Gloria con sal 200 g", "6.00", 10, "Lácteos y Derivados"),
            new SeedProduct("Leche chocolatada Chocolisto 200 ml", "2.00", 28, "Lácteos y Derivados"),
            new SeedProduct("Arroz extra Costeño 1 kg", "4.80", 42, "Abarrotes Básicos"),
            new SeedProduct("Azúcar rubia Cartavio 1 kg", "4.00", 9, "Abarrotes Básicos"),
            new SeedProduct("Aceite vegetal Primor Clásico 1 L", "9.50", 4, "Abarrotes Básicos"),
            new SeedProduct("Fideos spaghetti Don Vittorio 500 g", "3.20", 26, "Abarrotes Básicos"),
            new SeedProduct("Atún en lata Florida 170 g", "6.00", 14, "Abarrotes Básicos"),
            new SeedProduct("Avena Santa Catalina 1 kg", "5.50", 11, "Abarrotes Básicos"),
            new SeedProduct("Lentejita Costeño 500 g", "4.50", 8, "Abarrotes Básicos"),
            new SeedProduct("Salsa de tomate Pomarola 150 g", "2.20", 19, "Abarrotes Básicos"),
            new SeedProduct("Mayonesa Alacena 95 g", "2.50", 31, "Abarrotes Básicos"),
            new SeedProduct("Café instantáneo Nescafé Tradición 50 g", "6.50", 7, "Abarrotes Básicos"),
            new SeedProduct("Detergente Bolívar Matic 1 kg", "11.50", 16, "Limpieza e Higiene"),
            new SeedProduct("Jabón de tocador Protex Avena 110 g", "2.50", 24, "Limpieza e Higiene"),
            new SeedProduct("Pasta dental Dento Clásica 75 ml", "3.00", 13, "Limpieza e Higiene"),
            new SeedProduct("Lejía Sapolio 1 L", "2.80", 21, "Limpieza e Higiene"),
            new SeedProduct("Papel higiénico Suave doble hoja (Paq. x4)", "5.50", 35, "Limpieza e Higiene"),
            new SeedProduct("Lavavajillas en pasta Ayudín Limón 200 g", "3.50", 17, "Limpieza e Higiene"),
            new SeedProduct("Shampoo Sedal Ceramidas 340 ml", "12.00", 6, "Limpieza e Higiene"),
            new SeedProduct("Galletas de soda San Jorge x6", "1.20", 22, "Snacks y Golosinas"),
            new SeedProduct("Papas fritas Lay's Clásicas 45 g", "2.00", 5, "Snacks y Golosinas"),
            new SeedProduct("Galletas Casino de Fresa 43 g", "0.80", 40, "Snacks y Golosinas"),
            new SeedProduct("Chocolate Sublime Clásico 30 g", "1.50", 55, "Snacks y Golosinas"),
            new SeedProduct("Galletas Morochas 30 g", "1.00", 30, "Snacks y Golosinas"),
            new SeedProduct("Cuaderno rayado Standford 100 hojas", "6.50", 14, "Librería y Escolares"),
            new SeedProduct("Lapicero Faber-Castell Azul 031", "1.00", 48, "Librería y Escolares"),
            new SeedProduct("Lápiz de grafito Artesco 2B", "1.00", 25, "Librería y Escolares"),
            new SeedProduct("Goma en barra UHU 21 g", "4.00", 9, "Librería y Escolares"),
            new SeedProduct("Ciento de Papel bond A4 Report", "5.00", 7, "Librería y Escolares"),
            new SeedProduct("Audífonos genéricos con cable", "15.00", 4, "Regalos y Tecnología"),
            new SeedProduct("Crema corporal Nivea Milk 250 ml", "18.00", 3, "Regalos y Tecnología"),
            new SeedProduct("Desodorante Rexona Clinical Mujer 48 g", "16.50", 5, "Regalos y Tecnología"),
            new SeedProduct("Hot Dog San Fernando x3", "1.70", 0, "Embutidos"),
            new SeedProduct("Hot Dog San Fernando x6", "3.20", 0, "Embutidos"),
            new SeedProduct("Jamonada San Fernando", "1.70", 0, "Embutidos"),
            new SeedProduct("Chicharrón de prensa San Fernando", "1.60", 0, "Embutidos"),
            new SeedProduct("Chorizo San Fernando", "1.60", 0, "Embutidos")
    );
}
