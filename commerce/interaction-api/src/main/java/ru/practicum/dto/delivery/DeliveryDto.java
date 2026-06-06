package ru.practicum.dto.delivery;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.dto.warehouse.AddressDto;
import ru.practicum.enums.DeliveryState;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliveryDto {
    private UUID deliveryId;

    @Valid
    @NotNull
    private AddressDto fromAddress;

    @Valid
    @NotNull
    private AddressDto toAddress;

    @NotNull
    private UUID orderId;

    private DeliveryState deliveryState;
}
