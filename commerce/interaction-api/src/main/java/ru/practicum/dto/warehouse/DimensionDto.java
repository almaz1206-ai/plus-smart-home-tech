package ru.practicum.dto.warehouse;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class DimensionDto {
    @NotNull
    private Double width;

    @NotNull
    private Double height;

    @NotNull
    private Double depth;
}
