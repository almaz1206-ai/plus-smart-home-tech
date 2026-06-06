package ru.practicum.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.order.OrderDto;
import ru.practicum.dto.payment.PaymentDto;
import ru.practicum.dto.store.ProductDto;
import ru.practicum.enums.PaymentState;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.NoOrderFoundException;
import ru.practicum.exception.NotEnoughInfoInOrderToCalculateException;
import ru.practicum.feign.OrderClient;
import ru.practicum.feign.ShoppingStoreClient;
import ru.practicum.mapper.PaymentMapper;
import ru.practicum.model.Payment;
import ru.practicum.repository.PaymentRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {
    private static final BigDecimal FEE_RATE = BigDecimal.valueOf(0.10);

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final ShoppingStoreClient shoppingStoreClient;
    private final OrderClient orderClient;


    @Override
    @Transactional(readOnly = true)
    public BigDecimal productCost(OrderDto orderDto) {
        BigDecimal result = BigDecimal.ZERO;

        for (Map.Entry<UUID, Long> entry : orderDto.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            Long quantity = entry.getValue();

            ProductDto product = callFeign(
                () -> shoppingStoreClient.getProductById(productId),
        "Не удалось получить товар из shopping-store: " + productId
            );

            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));

            result = result.add(itemTotal);
        }

        return result.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal getTotalCost(OrderDto orderDto) {
        validateOrderForCalculation(orderDto);

        BigDecimal productTotal = productCost(orderDto);
        BigDecimal deliveryTotal = getDeliveryPrice(orderDto);
        BigDecimal feeTotal = calculateFee(productTotal);

        return productTotal.add(feeTotal).add(deliveryTotal).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public PaymentDto payment(OrderDto orderDto) {
        validateOrderForCalculation(orderDto);

        UUID orderId = orderDto.getOrderId();

        if (paymentRepository.existsByOrderId(orderId)) {
            throw new BadRequestException("Платеж для заказа уже существует: " + orderId);
        }


        BigDecimal productTotal = productCost(orderDto);
        BigDecimal deliveryTotal = getDeliveryPrice(orderDto);
        BigDecimal feeTotal = calculateFee(productTotal);
        BigDecimal totalPayment = productTotal.add(feeTotal).add(deliveryTotal).setScale(2, RoundingMode.HALF_UP);

        Payment payment = Payment.builder()
                .orderId(orderDto.getOrderId())
                .productTotal(productTotal)
                .deliveryTotal(deliveryTotal)
                .feeTotal(feeTotal)
                .totalPayment(totalPayment)
                .state(PaymentState.PENDING)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        return paymentMapper.toPaymentDto(savedPayment);
    }

    @Override
    public void paymentSuccess(UUID paymentId) {
        Payment payment = findPayment(paymentId);

        if (payment.getState() == PaymentState.SUCCESS) {
            throw new BadRequestException("Оплата уже подтверждена: " + paymentId);
        }

        if (payment.getState() == PaymentState.FAILED) {
            throw new BadRequestException("Нельзя подтвердить оплату со статусом FAILED: " + paymentId);
        }

        payment.setState(PaymentState.SUCCESS);
        paymentRepository.save(payment);

        callFeign(
            () -> orderClient.payment(payment.getOrderId()),
            "Не удалось обновить статус заказа после успешной оплаты"
        );
    }

    @Override
    public void paymentFailed(UUID paymentId) {
        Payment payment = findPayment(paymentId);

        if (payment.getState() == PaymentState.SUCCESS) {
            throw new BadRequestException("Нельзя отклонить уже успешную оплату: " + paymentId);
        }

        if (payment.getState() == PaymentState.FAILED) {
            throw new BadRequestException("Оплата уже отклонена: " + paymentId);
        }

        payment.setState(PaymentState.FAILED);
        paymentRepository.save(payment);

        callFeignVoid(
            () -> orderClient.paymentFailed(payment.getOrderId()),
            "Не удалось обновить статус заказа после ошибки оплаты"
        );
    }

    private Payment findPayment(UUID paymentId) {
        if (paymentId == null) {
            throw new NoOrderFoundException("Идентификатор оплаты не указан");
        }

        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoOrderFoundException(
                        "Оплата не найдена: " + paymentId
                ));
    }

    private void validateOrderForCalculation(OrderDto orderDto) {
        if (orderDto == null
                || orderDto.getOrderId() == null
                || orderDto.getProducts() == null
                || orderDto.getProducts().isEmpty()) {
            throw new NotEnoughInfoInOrderToCalculateException(
                    "Недостаточно информации в заказе для расчёта"
            );
        }
    }

    private BigDecimal getDeliveryPrice(OrderDto orderDto) {
        if (orderDto.getDeliveryPrice() == null) {
            throw new NotEnoughInfoInOrderToCalculateException(
                    "Не указана стоимость доставки"
            );
        }

        return orderDto.getDeliveryPrice().setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateFee(BigDecimal productTotal) {
        return productTotal
                .multiply(FEE_RATE)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private <T> T callFeign(Supplier<T> call, String message) {
        try {
            return call.get();
        } catch (FeignException.NotFound e) {
            throw new BadRequestException(message + ": ресурс не найден");
        } catch (FeignException.BadRequest e) {
            throw new BadRequestException(message + ": некорректный запрос");
        } catch (FeignException.Conflict e) {
            throw new BadRequestException(message + ": конфликт данных");
        } catch (FeignException e) {
            throw new BadRequestException(message + ": ошибка взаимодействия с другим сервисом");
        }
    }

    private void callFeignVoid(Runnable call, String message) {
        try {
            call.run();
        } catch (FeignException.NotFound e) {
            throw new BadRequestException(message + ": ресурс не найден");
        } catch (FeignException.BadRequest e) {
            throw new BadRequestException(message + ": некорректный запрос");
        } catch (FeignException.Conflict e) {
            throw new BadRequestException(message + ": конфликт данных");
        } catch (FeignException e) {
            throw new BadRequestException(message + ": ошибка взаимодействия с другим сервисом");
        }
    }
}
