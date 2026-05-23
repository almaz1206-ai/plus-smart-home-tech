package ru.practicum.feign;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.store.PageProductDto;
import ru.practicum.dto.store.ProductDto;
import ru.practicum.enums.ProductCategory;
import ru.practicum.enums.QuantityState;

import java.util.UUID;


public interface ShoppingStoreContract {

    PageProductDto getProducts(@RequestParam ProductCategory productCategory,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(defaultValue = "productName,asc") String sort);

    ProductDto getProductById(@PathVariable UUID productId);

    ProductDto createProduct(@Valid @RequestBody ProductDto productDto);

    ProductDto updateProduct(@Valid @RequestBody ProductDto productDto);

    void deleteProductById(@RequestBody UUID productId);

    Boolean updateQuantityState(@RequestParam UUID productId, @RequestParam QuantityState quantityState);
}
