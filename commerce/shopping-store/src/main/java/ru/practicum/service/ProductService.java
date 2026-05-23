package ru.practicum.service;

import ru.practicum.dto.store.PageProductDto;
import ru.practicum.dto.store.PageableObject;
import ru.practicum.dto.store.ProductDto;
import ru.practicum.enums.ProductCategory;
import ru.practicum.enums.QuantityState;

import java.util.UUID;

public interface ProductService {
    PageProductDto getProducts(ProductCategory productCategory, PageableObject pageable);

    ProductDto getProductById(UUID productId);

    ProductDto createProduct(ProductDto productDto);

    ProductDto updateProduct(ProductDto productDto);

    void deleteProductById(UUID productId);

    boolean updateQuantityState(UUID productId, QuantityState quantityState);

}
