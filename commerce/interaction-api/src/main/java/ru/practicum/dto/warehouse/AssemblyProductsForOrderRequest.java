package ru.practicum.dto.warehouse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssemblyProductsForOrderRequest {
    private UUID orderId;

    private Map<UUID, Long> products;
}
