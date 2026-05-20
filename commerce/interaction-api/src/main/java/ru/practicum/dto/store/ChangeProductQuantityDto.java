package ru.practicum.dto.store;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangeProductQuantityDto {
    @NotNull
    private UUID productId;

    @NotNull
    @Min(value = 0)
    private Long newQuantity;
}
