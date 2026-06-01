package ru.practicum.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.delivery.DeliveryDto;
import ru.practicum.dto.order.OrderDto;
import ru.practicum.dto.warehouse.AddressDto;
import ru.practicum.dto.warehouse.ShippedToDeliveryRequest;
import ru.practicum.enums.DeliveryState;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.NoDeliveryFoundException;
import ru.practicum.exception.NotEnoughInfoInOrderToCalculateException;
import ru.practicum.feign.OrderClient;
import ru.practicum.feign.WarehouseClient;
import ru.practicum.mapper.DeliveryMapper;
import ru.practicum.model.Delivery;
import ru.practicum.repository.DeliveryRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryServiceImpl implements DeliveryService {

    private static final BigDecimal BASE_COST = BigDecimal.valueOf(5.0);
    private static final BigDecimal ADDRESS_2_MULTIPLIER = BigDecimal.valueOf(2.0);
    private static final BigDecimal FRAGILE_RATE = BigDecimal.valueOf(0.2);
    private static final BigDecimal WEIGHT_RATE = BigDecimal.valueOf(0.3);
    private static final BigDecimal VOLUME_RATE = BigDecimal.valueOf(0.2);
    private static final BigDecimal DIFFERENT_STREET_RATE = BigDecimal.valueOf(0.2);

    private final DeliveryRepository deliveryRepository;
    private final DeliveryMapper deliveryMapper;
    private final OrderClient orderClient;
    private final WarehouseClient warehouseClient;

    @Override
    public DeliveryDto planDelivery(DeliveryDto deliveryDto) {
        Delivery delivery = deliveryMapper.toDelivery(deliveryDto);
        delivery.setDeliveryId(null);

        if (delivery.getDeliveryState() == null) {
            delivery.setDeliveryState(DeliveryState.CREATED);
        }

        Delivery savedDelivery = deliveryRepository.save(delivery);

        return deliveryMapper.toDeliveryDto(savedDelivery);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal deliveryCost(OrderDto orderDto) {
        validateOrderForDeliveryCost(orderDto);

        Delivery delivery = deliveryRepository.findByOrderId(orderDto.getOrderId())
                .orElseThrow(() -> new NoDeliveryFoundException(
                        "Доставка для заказа не найдена: " + orderDto.getOrderId()
                ));

        AddressDto warehouseAddress = deliveryMapperToAddressDto(delivery.getFromAddress());
        AddressDto toAddress = deliveryMapperToAddressDto(delivery.getToAddress());

        BigDecimal result = BASE_COST;

        if (containsAddress2(warehouseAddress)) {
            result = result.add(BASE_COST.multiply(ADDRESS_2_MULTIPLIER));
        } else {
            result = result.add(BASE_COST);
        }

        if (Boolean.TRUE.equals(orderDto.getFragile())) {
            result = result.add(result.multiply(FRAGILE_RATE));
        }

        result = result.add(BigDecimal.valueOf(orderDto.getDeliveryWeight()).multiply(WEIGHT_RATE));
        result = result.add(BigDecimal.valueOf(orderDto.getDeliveryVolume()).multiply(VOLUME_RATE));

        if (!sameStreet(warehouseAddress, toAddress)) {
            result = result.add(result.multiply(DIFFERENT_STREET_RATE));
        }

        return result.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public void deliveryPicked(UUID orderId) {
        Delivery delivery = findByOrderId(orderId);

        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        deliveryRepository.save(delivery);

        orderClient.assembly(orderId);


          callFeignVoid(
              () -> warehouseClient.shippedToDelivery(
                new ShippedToDeliveryRequest(orderId, delivery.getDeliveryId())
              ),
              "Не удалось передать товары в доставку на складе"
          );
    }

    @Override
    public void deliverySuccessful(UUID orderId) {
        Delivery delivery = findByOrderId(orderId);

        delivery.setDeliveryState(DeliveryState.DELIVERED);
        deliveryRepository.save(delivery);

        callFeignVoid(
            () -> orderClient.delivery(orderId),
            "Не удалось сообщить order сервису об успешной доставке"
        );
    }

    @Override
    public void deliveryFailed(UUID orderId) {
        Delivery delivery = findByOrderId(orderId);

        delivery.setDeliveryState(DeliveryState.FAILED);
        deliveryRepository.save(delivery);

        callFeignVoid(
            () -> orderClient.deliveryFailed(orderId),
            "Не удалось сообщить order-сервису об ошибке доставки"
        );
    }

    private Delivery findByOrderId(UUID orderId) {
        if (orderId == null) {
            throw new NoDeliveryFoundException("Идентификатор заказа не указан");
        }

        return deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoDeliveryFoundException(
                        "Доставка для заказа не найдена: " + orderId
                ));
    }

    private void validateOrderForDeliveryCost(OrderDto orderDto) {
        if (orderDto == null
                || orderDto.getOrderId() == null
                || orderDto.getDeliveryWeight() == null
                || orderDto.getDeliveryVolume() == null) {
            throw new NotEnoughInfoInOrderToCalculateException(
                    "Недостаточно информации в заказе для расчёта доставки"
            );
        }
    }

    private boolean containsAddress2(AddressDto address) {
        if (address == null) {
            return false;
        }

        String fullAddress = String.join(" ",
                nullToEmpty(address.getCountry()),
                nullToEmpty(address.getCity()),
                nullToEmpty(address.getStreet()),
                nullToEmpty(address.getHouse()),
                nullToEmpty(address.getFlat())
        );

        return fullAddress.contains("ADDRESS_2");
    }

    private boolean sameStreet(AddressDto fromAddress, AddressDto toAddress) {
        if (fromAddress == null || toAddress == null) {
            return false;
        }

        if (fromAddress.getStreet() == null || toAddress.getStreet() == null) {
            return false;
        }

        return fromAddress.getStreet().equalsIgnoreCase(toAddress.getStreet());
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private AddressDto deliveryMapperToAddressDto(ru.practicum.model.Address address) {
        if (address == null) {
            return null;
        }

        return AddressDto.builder()
                .country(address.getCountry())
                .city(address.getCity())
                .street(address.getStreet())
                .house(address.getHouse())
                .flat(address.getFlat())
                .build();
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