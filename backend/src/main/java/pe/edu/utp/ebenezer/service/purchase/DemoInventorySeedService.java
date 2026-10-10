package pe.edu.utp.ebenezer.service.purchase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.domain.entity.Category;
import pe.edu.utp.ebenezer.domain.entity.InventoryMovement;
import pe.edu.utp.ebenezer.domain.entity.Lot;
import pe.edu.utp.ebenezer.domain.entity.Product;
import pe.edu.utp.ebenezer.domain.entity.Promotion;
import pe.edu.utp.ebenezer.domain.entity.Purchase;
import pe.edu.utp.ebenezer.domain.entity.PurchaseDetail;
import pe.edu.utp.ebenezer.domain.entity.UnitOfMeasure;
import pe.edu.utp.ebenezer.domain.entity.User;
import pe.edu.utp.ebenezer.domain.enums.InventoryMovementType;
import pe.edu.utp.ebenezer.domain.enums.PurchaseStatus;
import pe.edu.utp.ebenezer.domain.enums.RoleName;
import pe.edu.utp.ebenezer.domain.repository.category.CategoryRepository;
import pe.edu.utp.ebenezer.domain.repository.inventory.InventoryMovementRepository;
import pe.edu.utp.ebenezer.domain.repository.inventory.LotRepository;
import pe.edu.utp.ebenezer.domain.repository.product.ProductRepository;
import pe.edu.utp.ebenezer.domain.repository.product.PromotionRepository;
import pe.edu.utp.ebenezer.domain.repository.purchase.PurchaseRepository;
import pe.edu.utp.ebenezer.domain.repository.supplier.SupplierRepository;
import pe.edu.utp.ebenezer.domain.repository.unit.UnitOfMeasureRepository;
import pe.edu.utp.ebenezer.domain.repository.user.UserRepository;

/**
 * Demo data to see inventory and sales with real lots: purchases per demo supplier (lots with near, far,
 * past or no expiration), two bulk products sold by weight and a few promotions. Idempotent: purchases
 * are skipped once the marker purchase exists, products and promotions once they exist.
 */
@Service
@RequiredArgsConstructor
public class DemoInventorySeedService {

    static final String SEED_NOTES = "Demo purchases v1";

    private final PurchaseRepository purchaseRepository;
    private final LotRepository lotRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final SupplierRepository supplierRepository;
    private final PromotionRepository promotionRepository;
    private final UserRepository userRepository;

    @Transactional
    public boolean seedMissingDemoInventory() {
        boolean changed = seedBulkProducts();
        if (!purchaseRepository.existsByNotes(SEED_NOTES)) {
            User admin = userRepository.findFirstByRole_NameAndActiveTrueOrderByIdAsc(RoleName.ADMIN)
                    .orElseThrow(() -> new IllegalStateException(
                            "Demo inventory requires an active ADMIN user"));
            for (PurchaseSeed seed : DEMO_PURCHASES) {
                changed |= seedPurchase(seed, admin);
            }
        }
        changed |= seedPromotions();
        return changed;
    }

    // Products sold by weight (base unit KG), only when the KG unit from database/002_seed.sql exists.
    private boolean seedBulkProducts() {
        Optional<UnitOfMeasure> kilogram = unitOfMeasureRepository.findByAbbreviation("KG");
        if (kilogram.isEmpty()) {
            return false;
        }
        boolean changed = false;
        for (BulkProductSeed seed : BULK_PRODUCTS) {
            Optional<Category> category = categoryRepository.findByName(seed.category());
            if (category.isEmpty() || productRepository.findByNameIgnoreCase(seed.name()).isPresent()) {
                continue;
            }
            Product product = new Product();
            product.setCategory(category.get());
            product.setBaseUnit(kilogram.get());
            product.setName(seed.name());
            product.setSalePrice(new BigDecimal(seed.price()));
            product.setMinStock(new BigDecimal(seed.minStock()));
            product.setActive(true);
            productRepository.save(product);
            changed = true;
        }
        return changed;
    }

    private boolean seedPurchase(PurchaseSeed seed, User admin) {
        LocalDateTime purchaseDate = LocalDateTime.now().minusDays(seed.daysAgo());
        Purchase purchase = new Purchase();
        purchase.setSupplier(seed.supplierName() == null
                ? null
                : supplierRepository.findByNameIgnoreCase(seed.supplierName()).orElse(null));
        purchase.setUser(admin);
        purchase.setPurchaseDate(purchaseDate);
        purchase.setStatus(PurchaseStatus.REGISTERED);
        purchase.setNotes(SEED_NOTES);

        List<Lot> lots = new ArrayList<>();
        List<InventoryMovement> movements = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (LineSeed line : seed.lines()) {
            Optional<Product> found = productRepository.findByNameIgnoreCase(line.productName());
            if (found.isEmpty()) {
                continue;
            }
            Product product = found.get();
            if (product.getMinStock().signum() == 0) {
                product.setMinStock(new BigDecimal(line.minStock()));
            }
            BigDecimal quantity = new BigDecimal(line.quantity());
            BigDecimal unitCost = new BigDecimal(line.unitCost());

            PurchaseDetail detail = new PurchaseDetail();
            detail.setPurchase(purchase);
            detail.setProduct(product);
            detail.setUnitOfMeasure(product.getBaseUnit());
            detail.setQuantity(quantity);
            detail.setBaseQuantity(quantity);
            detail.setUnitCost(unitCost);
            detail.setSubtotal(quantity.multiply(unitCost).setScale(2, RoundingMode.HALF_UP));
            purchase.getDetails().add(detail);
            total = total.add(detail.getSubtotal());

            Lot lot = new Lot();
            lot.setProduct(product);
            lot.setPurchaseDetail(detail);
            lot.setEntryDate(purchaseDate);
            lot.setExpirationDate(line.expiresInDays() == null
                    ? null
                    : LocalDate.now().plusDays(line.expiresInDays()));
            lots.add(lot);

            InventoryMovement movement = new InventoryMovement();
            movement.setProduct(product);
            movement.setLot(lot);
            movement.setUser(admin);
            movement.setMovementType(InventoryMovementType.PURCHASE);
            movement.setBaseQuantity(quantity);
            movement.setPurchaseDetail(detail);
            movement.setMovementDate(purchaseDate);
            movements.add(movement);
        }
        if (purchase.getDetails().isEmpty()) {
            return false;
        }
        purchase.setSubtotal(total);
        purchase.setTotal(total);
        purchaseRepository.save(purchase);
        lotRepository.saveAll(lots);
        inventoryMovementRepository.saveAll(movements);
        return true;
    }

    private boolean seedPromotions() {
        boolean changed = false;
        for (PromotionSeed seed : DEMO_PROMOTIONS) {
            Optional<Product> product = productRepository.findByNameIgnoreCase(seed.productName());
            Optional<UnitOfMeasure> unit = unitOfMeasureRepository.findByAbbreviation(seed.unit());
            if (product.isEmpty() || unit.isEmpty() || promotionRepository.existsByProduct_Id(product.get().getId())) {
                continue;
            }
            Promotion promotion = new Promotion();
            promotion.setProduct(product.get());
            promotion.setUnitOfMeasure(unit.get());
            promotion.setName(seed.name());
            promotion.setPromotionQuantity(new BigDecimal(seed.quantity()));
            promotion.setPromotionalPrice(new BigDecimal(seed.price()));
            promotion.setRepeatable(true);
            promotion.setActive(true);
            promotionRepository.save(promotion);
            changed = true;
        }
        return changed;
    }

    private record BulkProductSeed(String name, String price, String minStock, String category) {
    }

    /** {@code expiresInDays}: null = does not expire, negative = already expired. Quantities in the base unit. */
    private record LineSeed(String productName, String quantity, String unitCost, Integer expiresInDays,
            String minStock) {
    }

    private record PurchaseSeed(String supplierName, int daysAgo, List<LineSeed> lines) {
    }

    private record PromotionSeed(String productName, String unit, String quantity, String price, String name) {
    }

    private static final List<BulkProductSeed> BULK_PRODUCTS = List.of(
            new BulkProductSeed("Arroz a granel", "4.20", "5", "Abarrotes Básicos"),
            new BulkProductSeed("Huevos rosados a granel", "8.50", "3", "Abarrotes Básicos"));

    private static final List<PurchaseSeed> DEMO_PURCHASES = List.of(
            new PurchaseSeed("Arca Continental Lindley", 3, List.of(
                    new LineSeed("Gaseosa Inca Kola 500 ml", "24", "1.80", 120, "12"),
                    new LineSeed("Agua mineral San Luis 625 ml", "12", "0.90", 200, "10"),
                    new LineSeed("Bebida rehidratante Sporade 500 ml", "12", "1.40", 90, "6"))),
            new PurchaseSeed("Distribuidora Limpieza Norte", 6, List.of(
                    new LineSeed("Detergente Bolívar Matic 1 kg", "6", "9.20", null, "4"),
                    new LineSeed("Lejía Sapolio 1 L", "12", "2.00", null, "6"),
                    new LineSeed("Jabón de tocador Protex Avena 110 g", "12", "1.80", null, "6"))),
            new PurchaseSeed("Corporación Vega", 8, List.of(
                    new LineSeed("Arroz extra Costeño 1 kg", "20", "3.90", 180, "10"),
                    new LineSeed("Azúcar rubia Cartavio 1 kg", "10", "3.30", 240, "10"),
                    new LineSeed("Aceite vegetal Primor Clásico 1 L", "6", "8.10", 150, "12"),
                    new LineSeed("Arroz a granel", "25", "3.40", 180, "5"),
                    new LineSeed("Huevos rosados a granel", "10", "7.20", 12, "3"))),
            new PurchaseSeed("Distribuidora San Fernando", 20, List.of(
                    new LineSeed("Hot Dog San Fernando x3", "10", "1.20", 6, "4"),
                    new LineSeed("Hot Dog San Fernando x6", "8", "2.40", 6, "4"),
                    new LineSeed("Jamonada San Fernando", "6", "1.20", 4, "3"),
                    new LineSeed("Chicharrón de prensa San Fernando", "5", "1.10", 3, "3"),
                    // Already expired: shows "Vencido" in sales and EXPIRED in inventory.
                    new LineSeed("Chorizo San Fernando", "6", "1.10", -1, "3"))),
            new PurchaseSeed("Galerías Mercado Central", 12, List.of(
                    new LineSeed("Cuaderno rayado Standford 100 hojas", "10", "4.80", null, "5"),
                    new LineSeed("Lapicero Faber-Castell Azul 031", "24", "0.60", null, "12"),
                    new LineSeed("Audífonos genéricos con cable", "3", "9.00", null, "2"))),
            // Occasional purchase without supplier.
            new PurchaseSeed(null, 15, List.of(
                    new LineSeed("Yogurt de fresa Gloria 1 L", "6", "5.10", 3, "6"),
                    new LineSeed("Queso fresco Laive 250 g", "4", "6.20", 2, "4"),
                    new LineSeed("Leche evaporada Gloria Azul 400 g", "12", "3.50", 90, "12"),
                    new LineSeed("Galletas Casino de Fresa 43 g", "24", "0.55", 60, "12"),
                    new LineSeed("Chocolate Sublime Clásico 30 g", "24", "1.05", 45, "12"))));

    private static final List<PromotionSeed> DEMO_PROMOTIONS = List.of(
            new PromotionSeed("Galletas Casino de Fresa 43 g", "UND", "3", "2.00", "3 x S/ 2.00"),
            new PromotionSeed("Chocolate Sublime Clásico 30 g", "UND", "2", "2.50", "2 x S/ 2.50"),
            new PromotionSeed("Arroz a granel", "G", "500", "2.00", "1/2 kg x S/ 2.00"));
}
