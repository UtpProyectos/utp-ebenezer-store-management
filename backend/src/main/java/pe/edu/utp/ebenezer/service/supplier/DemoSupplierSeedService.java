package pe.edu.utp.ebenezer.service.supplier;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.domain.entity.Product;
import pe.edu.utp.ebenezer.domain.entity.Supplier;
import pe.edu.utp.ebenezer.domain.enums.SupplierType;
import pe.edu.utp.ebenezer.domain.repository.product.ProductRepository;
import pe.edu.utp.ebenezer.domain.repository.supplier.SupplierRepository;

@Service
@RequiredArgsConstructor
public class DemoSupplierSeedService {

    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    @Transactional
    public boolean seedMissingSuppliersAndProducts() {
        boolean changed = false;
        for (SupplierSeed seed : DEMO_SUPPLIERS) {
            Optional<Supplier> existingSupplier = supplierRepository.findByNameIgnoreCase(seed.name());
            Supplier supplier;
            if (existingSupplier.isPresent()) {
                supplier = existingSupplier.get();
            } else {
                supplier = supplierRepository.save(toEntity(seed));
                changed = true;
            }
            if (supplier.getProducts().isEmpty()) {
                List<Product> products = seed.productNames().stream()
                        .map(productRepository::findByNameIgnoreCase)
                        .flatMap(Optional::stream)
                        .toList();
                if (!products.isEmpty()) {
                    supplier.getProducts().addAll(products);
                    supplierRepository.save(supplier);
                    changed = true;
                }
            }
        }
        return changed;
    }

    private static Supplier toEntity(SupplierSeed seed) {
        Supplier supplier = new Supplier();
        supplier.setName(seed.name());
        supplier.setPhone(seed.phone());
        supplier.setContactName(seed.contactName());
        supplier.setAddress(seed.address());
        supplier.setNotes(seed.notes());
        supplier.setType(seed.type());
        supplier.setActive(true);
        return supplier;
    }

    private record SupplierSeed(
            String name,
            String phone,
            String contactName,
            String address,
            String notes,
            SupplierType type,
            List<String> productNames
    ) {
    }

    private static final List<SupplierSeed> DEMO_SUPPLIERS = List.of(
            new SupplierSeed("Arca Continental Lindley", "981 234 567", "Carlos Mendoza (Preventista)",
                    "Planta Moche / Ruta Comas", "Entrega en camión los días lunes y jueves.", SupplierType.DISTRIBUTOR,
                    List.of("Gaseosa Inca Kola 500 ml", "Agua mineral San Luis 625 ml",
                            "Bebida rehidratante Sporade 500 ml")),
            new SupplierSeed("Distribuidora Limpieza Norte", "945 678 123", "Rosa Ramírez",
                    "Parque Industrial - Comas", "Visita los miércoles al mediodía.", SupplierType.WHOLESALER,
                    List.of("Detergente Bolívar Matic 1 kg", "Lejía Sapolio 1 L",
                            "Jabón de tocador Protex Avena 110 g")),
            new SupplierSeed("Corporación Vega", "01 512 3456", "Miguel Soto",
                    "Av. Túpac Amaru 3140 - Comas",
                    "Pedidos por WhatsApp 48 horas antes. Entrega en furgoneta.", SupplierType.SELF_SERVICE,
                    List.of("Arroz extra Costeño 1 kg", "Azúcar rubia Cartavio 1 kg",
                            "Aceite vegetal Primor Clásico 1 L")),
            new SupplierSeed("Distribuidora San Fernando", "999 888 777", "Luis Vargas",
                    "Av. Trapiche - Comas", "Camión frigorífico. Llega los viernes por la mañana.",
                    SupplierType.DISTRIBUTOR,
                    List.of("Hot Dog San Fernando x3", "Hot Dog San Fernando x6", "Jamonada San Fernando",
                            "Chicharrón de prensa San Fernando", "Chorizo San Fernando")),
            new SupplierSeed("Galerías Mercado Central", "912 345 678", "Sra. Carmen (Varios stands)",
                    "Jr. Andahuaylas 800 - Cercado de Lima",
                    "Viaje presencial quincenal. Pago en efectivo.", SupplierType.MARKET,
                    List.of("Cuaderno rayado Standford 100 hojas", "Lapicero Faber-Castell Azul 031",
                            "Audífonos genéricos con cable"))
    );
}
