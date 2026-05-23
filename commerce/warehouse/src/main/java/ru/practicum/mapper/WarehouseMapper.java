package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.warehouse.NewProductInWareHouseDto;
import ru.practicum.model.Warehouse;

@Component
public class WarehouseMapper {
    public Warehouse toWarehouse(NewProductInWareHouseDto newProductInWareHouseDto) {
        return Warehouse.builder()
                .productId(newProductInWareHouseDto.getProductId())
                .dimensionDto(newProductInWareHouseDto.getDimension())
                .fragile(newProductInWareHouseDto.getFragile())
                .weight(newProductInWareHouseDto.getWeight())
                .build();
    }
}
