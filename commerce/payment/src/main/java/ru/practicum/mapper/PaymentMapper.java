package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.payment.PaymentDto;
import ru.practicum.model.Payment;

@Component
public class PaymentMapper {
    public PaymentDto toPaymentDto(Payment payment) {
        return PaymentDto.builder()
                .paymentId(payment.getPaymentId())
                .totalPayment(payment.getTotalPayment())
                .deliveryTotal(payment.getDeliveryTotal())
                .feeTotal(payment.getFeeTotal())
                .build();
    }
}
