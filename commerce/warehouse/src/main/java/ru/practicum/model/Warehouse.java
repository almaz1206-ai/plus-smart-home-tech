package ru.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.dto.warehouse.DimensionDto;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@Table(name = "warehouse")
public class Warehouse {
    @Id
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "fragile")
    private Boolean fragile;

    @Embedded
    private DimensionDto dimensionDto;

    @Column(name = "weight", nullable = false)
    private Double weight;

    @Column(name = "quantity")
    @Builder.Default
    private Long quantity = 0L;
}
