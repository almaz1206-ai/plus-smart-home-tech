package ru.practicum.dto.warehouse;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class NewProductInWareHouseDto {
    @NotNull
    private UUID productId;

    private Boolean fragile;

    @NotNull
    @JsonProperty("dimension")
    private DimensionDto dimension;

    @NotNull
    @Min(value = 1)
    private double weight;
}
