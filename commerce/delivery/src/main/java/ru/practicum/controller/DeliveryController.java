package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.delivery.DeliveryDto;
import ru.practicum.dto.order.OrderDto;
import ru.practicum.feign.DeliveryContract;
import ru.practicum.service.DeliveryService;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/delivery")
@RequiredArgsConstructor
public class DeliveryController implements DeliveryContract {
    private final DeliveryService deliveryService;

    @Override
    public DeliveryDto planDelivery(@RequestBody @Valid DeliveryDto deliveryDto) {
        return deliveryService.planDelivery(deliveryDto);
    }

    @Override
    public BigDecimal deliveryCost(@RequestBody @Valid OrderDto orderDto) {
        return deliveryService.deliveryCost(orderDto);
    }

    @Override
    public void deliveryPicked(@RequestBody UUID orderId) {
        deliveryService.deliveryPicked(orderId);
    }

    @Override
    public void deliverySuccessful(@RequestBody UUID orderId) {
        deliveryService.deliverySuccessful(orderId);
    }

    @Override
    public void deliveryFailed(@RequestBody UUID orderId) {
        deliveryService.deliveryFailed(orderId);
    }
}
