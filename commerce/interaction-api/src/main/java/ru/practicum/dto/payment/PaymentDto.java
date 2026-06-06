package ru.practicum.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentDto {
    private UUID paymentId;

    private BigDecimal deliveryTotal;

    private BigDecimal totalPayment;

    private BigDecimal feeTotal;
}
