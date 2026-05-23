package ru.practicum.service;

import ru.practicum.dto.cart.ShoppingCartDto;
import ru.practicum.dto.warehouse.AddProductToWarehouseDto;
import ru.practicum.dto.warehouse.AddressDto;
import ru.practicum.dto.warehouse.BookedProductsDto;
import ru.practicum.dto.warehouse.NewProductInWareHouseDto;

public interface WarehouseService {
    void addNewProductToWarehouse(NewProductInWareHouseDto newProductInWareHouseDto);

    BookedProductsDto checkProductQuantityInWarehouse(ShoppingCartDto shoppingCartDto);

    AddressDto getWarehouseAddress();

    void updateProductToWarehouse(AddProductToWarehouseDto addProductToWarehouseDto);
}
