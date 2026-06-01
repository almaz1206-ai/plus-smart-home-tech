package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.delivery.DeliveryDto;
import ru.practicum.dto.warehouse.AddressDto;
import ru.practicum.model.Address;
import ru.practicum.model.Delivery;

@Component
public class DeliveryMapper {

    public DeliveryDto toDeliveryDto(Delivery delivery) {
        return DeliveryDto.builder()
                .deliveryId(delivery.getDeliveryId())
                .orderId(delivery.getOrderId())
                .fromAddress(toAddressDto(delivery.getFromAddress()))
                .toAddress(toAddressDto(delivery.getToAddress()))
                .deliveryState(delivery.getDeliveryState())
                .build();
    }

    public Delivery toDelivery(DeliveryDto dto) {
        if (dto == null) {
            return null;
        }

        return Delivery.builder()
                .deliveryId(dto.getDeliveryId())
                .orderId(dto.getOrderId())
                .fromAddress(toAddress(dto.getFromAddress()))
                .toAddress(toAddress(dto.getToAddress()))
                .deliveryState(dto.getDeliveryState())
                .build();
    }

    private AddressDto toAddressDto(Address address) {
        return AddressDto.builder()
                .country(address.getCountry())
                .city(address.getCity())
                .street(address.getStreet())
                .house(address.getHouse())
                .flat(address.getFlat())
                .build();
    }

    private Address toAddress(AddressDto dto) {
        if (dto == null) {
            return null;
        }

        return Address.builder()
                .country(dto.getCountry())
                .city(dto.getCity())
                .street(dto.getStreet())
                .house(dto.getHouse())
                .flat(dto.getFlat())
                .build();
    }
}
