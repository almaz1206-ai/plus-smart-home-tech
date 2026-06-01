package ru.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.order.CreateNewOrderRequest;
import ru.practicum.dto.order.OrderDto;
import ru.practicum.dto.order.ProductReturnRequest;
import ru.practicum.feign.OrderContract;
import ru.practicum.service.OrderService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController implements OrderContract {
    private final OrderService orderService;

    public List<OrderDto> getClientOrders(@RequestParam @NotBlank String username) {
        return orderService.getClientOrders(username);
    }

    public OrderDto createNewOrder(@Valid @RequestBody CreateNewOrderRequest request) {
        return orderService.createNewOrder(request);
    }

    public OrderDto productReturn(@Valid @RequestBody ProductReturnRequest request) {
        return orderService.productReturn(request);
    }

    public OrderDto payment(@RequestBody UUID orderId) {
        return orderService.payment(orderId);
    }

    public OrderDto paymentFailed(@RequestBody UUID orderId) {
        return orderService.paymentFailed(orderId);
    }

    public OrderDto delivery(@RequestBody UUID orderId) {
        return orderService.delivery(orderId);
    }

    public OrderDto deliveryFailed(@RequestBody UUID orderId) {
        return orderService.deliveryFailed(orderId);
    }

    public OrderDto complete(@RequestBody UUID orderId) {
        return orderService.complete(orderId);
    }

    public OrderDto calculateTotalCost(@RequestBody UUID orderId) {
        return orderService.calculateTotalCost(orderId);
    }

    public OrderDto calculateDeliveryCost(@RequestBody UUID orderId) {
        return orderService.calculateDeliveryCost(orderId);
    }

    public OrderDto assembly(@RequestBody UUID orderId) {
        return orderService.assembly(orderId);
    }

    public OrderDto assemblyFailed(@RequestBody UUID orderId) {
        return orderService.assemblyFailed(orderId);
    }
}
