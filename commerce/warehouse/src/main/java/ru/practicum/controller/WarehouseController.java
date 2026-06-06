package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.cart.ShoppingCartDto;
import ru.practicum.dto.warehouse.*;
import ru.practicum.feign.WarehouseContract;
import ru.practicum.service.WarehouseService;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/warehouse")
public class WarehouseController implements WarehouseContract {
    private final WarehouseService warehouseService;

    @Override
    public void addNewProductToWarehouse(@RequestBody @Valid NewProductInWareHouseDto newProductInWareHouseDto) {
        warehouseService.addNewProductToWarehouse(newProductInWareHouseDto);
    }

    @Override
    public BookedProductsDto checkProductQuantityInWarehouse(@RequestBody @Valid ShoppingCartDto shoppingCartDto) {
        return warehouseService.checkProductQuantityInWarehouse(shoppingCartDto);
    }

    @Override
    public void updateProductToWarehouse(@RequestBody @Valid AddProductToWarehouseDto addProductToWarehouseDto) {
        warehouseService.updateProductToWarehouse(addProductToWarehouseDto);
    }

    @Override
    public AddressDto getWarehouseAddress() {
        return warehouseService.getWarehouseAddress();
    }

    @Override
    public BookedProductsDto assemblyProductForOrder(@RequestBody @Valid AssemblyProductsForOrderRequest request) {
        return warehouseService.assemblyProductForOrder(request);
    }

    @Override
    public void shippedToDelivery(@RequestBody @Valid ShippedToDeliveryRequest request) {
        warehouseService.shippedToDelivery(request);
    }

    @Override
    public void returnProducts(@RequestBody Map<UUID, Long> products) {
        warehouseService.returnProducts(products);
    }
}
