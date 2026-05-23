package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.cart.ShoppingCartDto;
import ru.practicum.dto.warehouse.AddProductToWarehouseDto;
import ru.practicum.dto.warehouse.AddressDto;
import ru.practicum.dto.warehouse.BookedProductsDto;
import ru.practicum.dto.warehouse.NewProductInWareHouseDto;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.NoSpecifiedProductInWarehouseException;
import ru.practicum.exception.ProductInShoppingCartLowQuantityInWarehouseException;
import ru.practicum.exception.SpecifiedProductAlreadyInWarehouseException;
import ru.practicum.mapper.WarehouseMapper;
import ru.practicum.model.Warehouse;
import ru.practicum.repository.WarehouseRepository;

import java.security.SecureRandom;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WarehouseServiceImpl implements WarehouseService {
    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper mapper;

    private static final String[] ADDRESSES =
            new String[] {"ADDRESS_1", "ADDRESS_2"};

    private final String CURRENT_ADDRESS =
            ADDRESSES[Random.from(new SecureRandom()).nextInt(0, ADDRESSES.length)];

    @Transactional
    @Override
    public void addNewProductToWarehouse(NewProductInWareHouseDto newProductInWareHouseDto) {
        UUID productId = newProductInWareHouseDto.getProductId();

        log.info("Запрос на добавление продукта с productId={}, dimension={} на склад",
                newProductInWareHouseDto.getProductId(), newProductInWareHouseDto.getDimension().getWidth());
        if (warehouseRepository.existsById(productId)) {
            throw new SpecifiedProductAlreadyInWarehouseException(String.format(
                    "Товар с id=%s уже есть на складе", productId
            ));
        }

        Warehouse warehouse = mapper.toWarehouse(newProductInWareHouseDto);
        warehouseRepository.save(warehouse);
    }

    @Override
    @Transactional(readOnly = true)
    public BookedProductsDto checkProductQuantityInWarehouse(ShoppingCartDto shoppingCartDto) {
        Set<UUID> productIds = shoppingCartDto.getProducts().keySet();

        if (productIds.isEmpty()) {
            return new BookedProductsDto(0.0, 0.0, false);
        }

        List<Warehouse> warehouses = warehouseRepository.findAllById(productIds);

        if (warehouses.size() != productIds.size()) {
            Set<UUID> foundIds = warehouses.stream()
                    .map(Warehouse::getProductId)
                    .collect(Collectors.toSet());

            Set<UUID> missingIds = productIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toSet());

            throw new NoSpecifiedProductInWarehouseException(String.format(
                    "Товары с id=%s не найдены на складе", missingIds
            ));
        }
        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean hasFragileItems = false;

        Map<UUID, Warehouse> warehouseMap = warehouses.stream()
                .collect(Collectors.toMap(Warehouse::getProductId, Function.identity()));

        for (Map.Entry<UUID, Long> warehouseEntry : shoppingCartDto.getProducts().entrySet()) {
            UUID productId = warehouseEntry.getKey();
            Long requestQuantity = warehouseEntry.getValue();

            if (requestQuantity == null || requestQuantity <= 0) {
                throw new BadRequestException(String.format("Некорректное число товара с id=%s, количество=%d",
                        productId, requestQuantity));
            }

            Warehouse warehouse = warehouseMap.get(productId);

            if (warehouse.getQuantity() < requestQuantity) {
                log.warn("");
                throw new ProductInShoppingCartLowQuantityInWarehouseException(String.format(
                        "Недостаточно товара на складе. Товара с id=%s, доступно=%d",
                        productId, warehouse.getQuantity()
                ));
            }

            totalWeight += warehouse.getWeight() * requestQuantity;

            totalVolume += warehouse.getDimensionDto().getWidth() * warehouse.getDimensionDto().getHeight() *
                    warehouse.getDimensionDto().getDepth() * requestQuantity;

            if (warehouse.getFragile()) {
                hasFragileItems = true;
            }
        }

        return new BookedProductsDto(totalWeight, totalVolume, hasFragileItems);
    }

    @Override
    public AddressDto getWarehouseAddress() {
        return AddressDto.builder()
                .country(CURRENT_ADDRESS)
                .city(CURRENT_ADDRESS)
                .street(CURRENT_ADDRESS)
                .house(CURRENT_ADDRESS)
                .flat(CURRENT_ADDRESS)
                .build();
    }

    @Override
    public void updateProductToWarehouse(AddProductToWarehouseDto addProductToWarehouseDto) {
        UUID productId = addProductToWarehouseDto.getProductId();

        Warehouse warehouse = warehouseRepository.findById(productId)
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(String.format(
                        "Товар с id=%s не найден на складе", productId
                )));
        warehouse.setQuantity(warehouse.getQuantity() + addProductToWarehouseDto.getQuantity());
        warehouseRepository.save(warehouse);
    }
}
