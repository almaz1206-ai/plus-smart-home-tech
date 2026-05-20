package ru.practicum.feign;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.dto.cart.ShoppingCartDto;
import ru.practicum.dto.warehouse.AddProductToWarehouseDto;
import ru.practicum.dto.warehouse.AddressDto;
import ru.practicum.dto.warehouse.BookedProductsDto;
import ru.practicum.dto.warehouse.NewProductInWareHouseDto;

public interface WarehouseContract {
    void addNewProductToWarehouse(@RequestBody @Valid NewProductInWareHouseDto request);

    BookedProductsDto checkProductQuantityInWarehouse(@RequestBody @Valid ShoppingCartDto shoppingCartDto);

    void updateProductToWarehouse(@RequestBody @Valid AddProductToWarehouseDto addProductToWarehouseDto);

    AddressDto getWarehouseAddress();
}
