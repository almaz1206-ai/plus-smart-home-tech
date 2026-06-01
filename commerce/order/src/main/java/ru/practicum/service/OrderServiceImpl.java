package ru.practicum.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.cart.ShoppingCartDto;
import ru.practicum.dto.delivery.DeliveryDto;
import ru.practicum.dto.order.CreateNewOrderRequest;
import ru.practicum.dto.order.OrderDto;
import ru.practicum.dto.order.ProductReturnRequest;
import ru.practicum.dto.payment.PaymentDto;
import ru.practicum.dto.warehouse.AddressDto;
import ru.practicum.dto.warehouse.AssemblyProductsForOrderRequest;
import ru.practicum.dto.warehouse.BookedProductsDto;
import ru.practicum.enums.DeliveryState;
import ru.practicum.enums.OrderState;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.NoOrderFoundException;
import ru.practicum.exception.NotEnoughInfoInOrderToCalculateException;
import ru.practicum.feign.DeliveryClient;
import ru.practicum.feign.PaymentClient;
import ru.practicum.feign.ShoppingCartClient;
import ru.practicum.feign.WarehouseClient;
import ru.practicum.mapper.OrderMapper;
import ru.practicum.model.Order;
import ru.practicum.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper mapper;
    private final DeliveryClient deliveryClient;
    private final PaymentClient paymentClient;
    private final WarehouseClient warehouseClient;
    private final ShoppingCartClient shoppingCartClient;

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getClientOrders(String username) {
        return orderRepository.findByUsername(username).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public OrderDto createNewOrder(CreateNewOrderRequest request) {
        if (request == null || request.getShoppingCart() == null) {
            throw new NotEnoughInfoInOrderToCalculateException(
                    "Недостаточно данных для создания заказа"
            );
        }

        ShoppingCartDto shoppingCart = request.getShoppingCart();

        if (shoppingCart.getProducts() == null || shoppingCart.getProducts().isEmpty()) {
            throw new NotEnoughInfoInOrderToCalculateException(
                    "Нельзя создать заказ из пустой корзины"
            );
        }

        BookedProductsDto bookedProducts = callFeign(
                () -> warehouseClient.checkProductQuantityInWarehouse(shoppingCart),
                "Не удалось проверить наличие товаров на складе"
        );

        AddressDto warehouseAddress = callFeign(
                warehouseClient::getWarehouseAddress,
                "Не удалось получить адрес склада"
        );

        Order order = Order.builder()
                .shoppingCartId(shoppingCart.getShoppingCartId())
                .products(new HashMap<>(shoppingCart.getProducts()))
                .state(OrderState.NEW)
                .deliveryWeight(bookedProducts.getDeliveryWeight())
                .deliveryVolume(bookedProducts.getDeliveryVolume())
                .fragile(bookedProducts.getFragile())
                .username(shoppingCart.getUsername())
                .build();

        Order savedOrder = orderRepository.save(order);

        DeliveryDto deliveryDto = DeliveryDto.builder()
                .orderId(savedOrder.getOrderId())
                .fromAddress(warehouseAddress)
                .toAddress(request.getAddressDto())
                .deliveryState(DeliveryState.CREATED)
                .build();

        DeliveryDto plannedDelivery = callFeign(
                () -> deliveryClient.planDelivery(deliveryDto),
                "Не удалось запланировать доставку"
        );

        savedOrder.setDeliveryId(plannedDelivery.getDeliveryId());

        return mapper.toDto(orderRepository.save(savedOrder));
    }

    @Override
    public OrderDto productReturn(ProductReturnRequest request) {
        if (request == null || request.getOrderId() == null) {
            throw new NoOrderFoundException("Идентификатор заказа не указан");
        }

        if (request.getProducts() == null || request.getProducts().isEmpty()) {
            throw new NotEnoughInfoInOrderToCalculateException(
                    "Не указан список товаров для возврата"
            );
        }

        Order order = findOrder(request.getOrderId());

        callFeignVoid(
                () -> warehouseClient.returnProducts(request.getProducts()),
                "Не удалось вернуть товары на склад"
        );

        order.setState(OrderState.PRODUCT_RETURNED);

        return mapper.toDto(orderRepository.save(order));
    }

    @Override
    public OrderDto payment(UUID orderId) {
        Order order = findOrder(orderId);

        if (order.getState() == OrderState.PAID) {
            throw new BadRequestException("Заказ уже оплачен");
        }

        if (order.getState() == OrderState.ON_PAYMENT) {
            order.setState(OrderState.PAID);
            return mapper.toDto(order);
        }

        if (!order.getState().equals(OrderState.ASSEMBLED)) {
            throw new BadRequestException(String.format("Заказ с id=%s ещё не собран", orderId));
        }

        PaymentDto paymentDto = callFeign(
                () -> paymentClient.payment(mapper.toDto(order)),
                "Не удалось создать оплату"
        );

        order.setPaymentId(paymentDto.getPaymentId());
        order.setState(OrderState.ON_PAYMENT);

        return mapper.toDto(orderRepository.save(order));
    }

    @Override
    public OrderDto paymentFailed(UUID orderId) {
        Order order = findOrder(orderId);

        order.setState(OrderState.PAYMENT_FAILED);

        return mapper.toDto(orderRepository.save(order));
    }

    @Override
    public OrderDto delivery(UUID orderId) {
        Order order = findOrder(orderId);

        order.setState(OrderState.DELIVERED);

        return mapper.toDto(orderRepository.save(order));
    }

    @Override
    public OrderDto deliveryFailed(UUID orderId) {
        Order order = findOrder(orderId);

        order.setState(OrderState.DELIVERY_FAILED);

        return mapper.toDto(orderRepository.save(order));
    }

    @Override
    public OrderDto complete(UUID orderId) {
        Order order = findOrder(orderId);

        order.setState(OrderState.COMPLETED);

        return mapper.toDto(orderRepository.save(order));
    }

    @Override
    public OrderDto calculateTotalCost(UUID orderId) {
        Order order = findOrder(orderId);

        OrderDto orderDto = mapper.toDto(order);

        BigDecimal productCost = callFeign(
                () -> paymentClient.productCost(orderDto),
                "Не удалось рассчитать стоимость товара"
                );
        order.setProductPrice(productCost);

        OrderDto orderDtoWithProductCost = mapper.toDto(order);

        BigDecimal totalCost = callFeign(
                () -> paymentClient.getTotalCost(orderDtoWithProductCost),
                "Не удалось рассчитать итоговую стоимость заказа"
                );

        order.setTotalPrice(totalCost);

        return mapper.toDto(orderRepository.save(order));
    }

    @Override
    public OrderDto calculateDeliveryCost(UUID orderId) {
        Order order = findOrder(orderId);

        BigDecimal deliveryCost = callFeign(
                () -> deliveryClient.deliveryCost(mapper.toDto(order)),
                "Не удалось рассчитать стоимость доставки"
                );

        order.setDeliveryPrice(deliveryCost);

        return mapper.toDto(orderRepository.save(order));
    }

    @Override
    public OrderDto assembly(UUID orderId) {
        Order order = findOrder(orderId);

        if (order.getProducts() == null || order.getProducts().isEmpty()) {
            throw new NotEnoughInfoInOrderToCalculateException(
                    "Нельзя собрать заказ без товаров"
            );
        }

        if (order.getState() == OrderState.ASSEMBLED) {
            throw new BadRequestException("Заказ уже собран");
        }

        if (order.getState() == OrderState.CANCELED
                || order.getState() == OrderState.PRODUCT_RETURNED
                || order.getState() == OrderState.COMPLETED) {
            throw new BadRequestException(
                    String.format("Заказ с id=%s нельзя собрать в статусе %s", orderId, order.getState())
            );
        }

        AssemblyProductsForOrderRequest request = new AssemblyProductsForOrderRequest(
                order.getOrderId(),
                order.getProducts()
        );

        BookedProductsDto bookedProducts = callFeign(
                () -> warehouseClient.assemblyProductForOrder(request),
                "Не удалось собрать товары на складе"
        );

        order.setDeliveryWeight(bookedProducts.getDeliveryWeight());
        order.setDeliveryVolume(bookedProducts.getDeliveryVolume());
        order.setFragile(bookedProducts.getFragile());
        order.setState(OrderState.ASSEMBLED);

        return mapper.toDto(orderRepository.save(order));
    }

    @Override
    public OrderDto assemblyFailed(UUID orderId) {
        Order order = findOrder(orderId);

        order.setState(OrderState.ASSEMBLY_FAILED);

        return mapper.toDto(orderRepository.save(order));
    }

    private Order findOrder(UUID orderId) {
        if (orderId == null) {
            throw new NoOrderFoundException("Идентификатор заказа не указан");
        }

        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException(
                        "Заказ не найден: " + orderId
                ));
    }

    private <T> T callFeign(Supplier<T> call, String message) {
        try {
            return call.get();
        } catch (FeignException.NotFound e) {
            throw new BadRequestException(message + ": ресурс не найден в связанном сервисе");
        } catch (FeignException.BadRequest e) {
            throw new BadRequestException(message + ": некорректный запрос в связанный сервис");
        } catch (FeignException.Conflict e) {
            throw new BadRequestException(message + ": конфликт данных в связанном сервисе");
        } catch (FeignException e) {
            throw new BadRequestException(message + ": ошибка взаимодействия с другим сервисом");
        }
    }

    private void callFeignVoid(Runnable call, String message) {
        try {
            call.run();
        } catch (FeignException.NotFound e) {
            throw new BadRequestException(message + ": ресурс не найден в связанном сервисе");
        } catch (FeignException.BadRequest e) {
            throw new BadRequestException(message + ": некорректный запрос в связанный сервис");
        } catch (FeignException.Conflict e) {
            throw new BadRequestException(message + ": конфликт данных в связанном сервисе");
        } catch (FeignException e) {
            throw new BadRequestException(message + ": ошибка взаимодействия с другим сервисом");
        }
    }
}
