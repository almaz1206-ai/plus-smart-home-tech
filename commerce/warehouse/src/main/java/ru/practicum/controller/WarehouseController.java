package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.cart.ShoppingCartDto;
import ru.practicum.dto.warehouse.AddProductToWarehouseDto;
import ru.practicum.dto.warehouse.AddressDto;
import ru.practicum.dto.warehouse.BookedProductsDto;
import ru.practicum.dto.warehouse.NewProductInWareHouseDto;
import ru.practicum.feign.WarehouseContract;
import ru.practicum.service.WarehouseService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/warehouse")
public class WarehouseController implements WarehouseContract {
    private final WarehouseService warehouseService;

    @Override
    @PutMapping
    public void addNewProductToWarehouse(@RequestBody @Valid NewProductInWareHouseDto newProductInWareHouseDto) {
        warehouseService.addNewProductToWarehouse(newProductInWareHouseDto);
    }

    @Override
    @PostMapping("/check")
    public BookedProductsDto checkProductQuantityInWarehouse(@RequestBody @Valid ShoppingCartDto shoppingCartDto) {
        return warehouseService.checkProductQuantityInWarehouse(shoppingCartDto);
    }

    @Override
    @PostMapping("/add")
    public void updateProductToWarehouse(@RequestBody @Valid AddProductToWarehouseDto addProductToWarehouseDto) {
        warehouseService.updateProductToWarehouse(addProductToWarehouseDto);
    }

    @Override
    @GetMapping("/address")
    public AddressDto getWarehouseAddress() {
        return warehouseService.getWarehouseAddress();
    }
}
