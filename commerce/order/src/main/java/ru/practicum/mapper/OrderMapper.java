package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.order.OrderDto;
import ru.practicum.model.Order;

import java.util.HashMap;

@Component
public class OrderMapper {
    public OrderDto toDto(Order order) {
        return OrderDto.builder()
                .orderId(order.getOrderId())
                .products(order.getProducts() == null ?
                        new HashMap<>() : new HashMap<>(order.getProducts()))
                .shoppingCartId(order.getShoppingCartId())
                .deliveryId(order.getDeliveryId())
                .paymentId(order.getPaymentId())
                .state(order.getState())
                .deliveryWeight(order.getDeliveryWeight())
                .deliveryVolume(order.getDeliveryVolume())
                .fragile(order.getFragile())
                .totalPrice(order.getTotalPrice())
                .productPrice(order.getProductPrice())
                .deliveryPrice(order.getDeliveryPrice())
                .build();
    }
}
