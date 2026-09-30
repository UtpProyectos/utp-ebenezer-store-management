package pe.edu.utp.ebenezer.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "business_settings")
public class BusinessSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "business_settings_id")
    private Long id;

    @Column(name = "business_name", nullable = false, length = 150)
    private String businessName;

    @Column(name = "ruc", length = 20)
    private String ruc;

    @Column(name = "address", length = 250)
    private String address;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency = "PEN";

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "default_min_stock", nullable = false, precision = 15, scale = 3)
    private BigDecimal defaultMinStock = BigDecimal.TEN;

    @Column(name = "expiration_warning_days", nullable = false)
    private Integer expirationWarningDays = 10;

    @Column(name = "critical_stock_alert", nullable = false)
    private Boolean criticalStockAlert = true;

    @Column(name = "expiration_alert", nullable = false)
    private Boolean expirationAlert = true;

    @Column(name = "auto_print_ticket", nullable = false)
    private Boolean autoPrintTicket = false;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
