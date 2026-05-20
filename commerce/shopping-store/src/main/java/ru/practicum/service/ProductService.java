package ru.practicum.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.dto.store.ProductDto;
import ru.practicum.enums.ProductCategory;
import ru.practicum.enums.QuantityState;

import java.util.UUID;

public interface ProductService {
    Page<ProductDto> getProducts(ProductCategory productCategory, Pageable pageable);

    ProductDto getProductById(UUID productId);

    ProductDto createProduct(ProductDto productDto);

    ProductDto updateProduct(ProductDto productDto);

    boolean deleteProductById(UUID productId);

    boolean updateQuantityState(UUID productId, QuantityState quantityState);

}
