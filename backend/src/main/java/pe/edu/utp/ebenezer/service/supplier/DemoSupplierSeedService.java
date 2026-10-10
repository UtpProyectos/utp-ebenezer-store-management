package pe.edu.utp.ebenezer.service.supplier;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.domain.entity.Supplier;
import pe.edu.utp.ebenezer.domain.enums.SupplierType;
import pe.edu.utp.ebenezer.domain.repository.supplier.SupplierRepository;

@Service
@RequiredArgsConstructor
public class DemoSupplierSeedService {

    private final SupplierRepository supplierRepository;

    @Transactional
    public boolean seedIfSuppliersAreEmpty() {
        if (supplierRepository.count() > 0) {
            return false;
        }

        supplierRepository.saveAll(DEMO_SUPPLIERS.stream().map(DemoSupplierSeedService::toEntity).toList());
        return true;
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
            SupplierType type
    ) {
    }

    private static final List<SupplierSeed> DEMO_SUPPLIERS = List.of(
            new SupplierSeed("Arca Continental Lindley", "981 234 567", "Carlos Mendoza (Preventista)",
                    "Planta Moche / Ruta Comas", "Entrega en camión los días lunes y jueves.", SupplierType.DISTRIBUTOR),
            new SupplierSeed("Distribuidora Limpieza Norte", "945 678 123", "Rosa Ramírez",
                    "Parque Industrial - Comas", "Visita los miércoles al mediodía.", SupplierType.WHOLESALER),
            new SupplierSeed("Corporación Vega", "01 512 3456", "Miguel Soto",
                    "Av. Túpac Amaru 3140 - Comas",
                    "Pedidos por WhatsApp 48 horas antes. Entrega en furgoneta.", SupplierType.SELF_SERVICE),
            new SupplierSeed("Distribuidora San Fernando", "999 888 777", "Luis Vargas",
                    "Av. Trapiche - Comas", "Camión frigorífico. Llega los viernes por la mañana.",
                    SupplierType.DISTRIBUTOR),
            new SupplierSeed("Galerías Mercado Central", "912 345 678", "Sra. Carmen (Varios stands)",
                    "Jr. Andahuaylas 800 - Cercado de Lima",
                    "Viaje presencial quincenal. Pago en efectivo.", SupplierType.MARKET)
    );
}
