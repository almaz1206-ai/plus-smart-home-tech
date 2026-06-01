package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.order.OrderDto;
import ru.practicum.dto.payment.PaymentDto;
import ru.practicum.feign.PaymentContract;
import ru.practicum.service.PaymentService;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController implements PaymentContract {
    private final PaymentService paymentService;

    @Override
    public BigDecimal productCost(@RequestBody @Valid OrderDto orderDto) {
        return paymentService.productCost(orderDto);
    }

    @Override
    public BigDecimal getTotalCost(@RequestBody @Valid OrderDto orderDto) {
        return paymentService.getTotalCost(orderDto);
    }

    @Override
    public PaymentDto payment(@RequestBody @Valid OrderDto orderDto) {
        return paymentService.payment(orderDto);
    }

    @Override
    public void paymentSuccess(@RequestBody UUID paymentId) {
        paymentService.paymentSuccess(paymentId);
    }

    @Override
    public void paymentFailed(@RequestBody UUID paymentId) {
        paymentService.paymentFailed(paymentId);
    }

}
