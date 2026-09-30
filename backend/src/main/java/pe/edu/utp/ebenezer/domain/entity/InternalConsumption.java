package pe.edu.utp.ebenezer.domain.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "internal_consumption")
public class InternalConsumption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_consumption_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "consumption_date", nullable = false)
    private LocalDateTime consumptionDate;

    @Column(name = "reason", length = 300)
    private String reason;

    @Column(name = "notes", length = 500)
    private String notes;

    // Header -> detail only. No CascadeType.REMOVE: records are never physically deleted in cascade.
    @OneToMany(mappedBy = "internalConsumption", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<InternalConsumptionDetail> details = new ArrayList<>();
}
