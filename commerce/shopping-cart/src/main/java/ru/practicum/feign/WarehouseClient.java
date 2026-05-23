package ru.practicum.feign;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.dto.cart.ShoppingCartDto;
import ru.practicum.dto.warehouse.AddProductToWarehouseDto;
import ru.practicum.dto.warehouse.AddressDto;
import ru.practicum.dto.warehouse.BookedProductsDto;
import ru.practicum.dto.warehouse.NewProductInWareHouseDto;

@FeignClient(name = "warehouse", path = "/api/v1/warehouse")
public interface WarehouseClient extends WarehouseContract {
    @Override
    @PutMapping
    void addNewProductToWarehouse(@RequestBody @Valid NewProductInWareHouseDto request);

    @Override
    @PostMapping("/check")
    BookedProductsDto checkProductQuantityInWarehouse(@RequestBody @Valid ShoppingCartDto shoppingCartDto);

    @Override
    @PostMapping("/add")
    void  updateProductToWarehouse(@RequestBody @Valid AddProductToWarehouseDto addProductToWarehouseDto);

    @Override
    @GetMapping("/address")
    AddressDto getWarehouseAddress();
}
