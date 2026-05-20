package ru.practicum.feign;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.store.ProductDto;
import ru.practicum.enums.ProductCategory;
import ru.practicum.enums.QuantityState;

import java.util.UUID;


public interface ShoppingStoreContract {

    Page<ProductDto> getProducts(@RequestParam ProductCategory productCategory, Pageable pageable);

    ProductDto getProductById(@PathVariable UUID productId);

    ProductDto createProduct(@Valid @RequestBody ProductDto productDto);

    ProductDto updateProduct(@Valid @RequestBody ProductDto productDto);

    Boolean deleteProductById(@RequestBody UUID productId);

    Boolean updateQuantityState(@RequestParam UUID productId, @RequestParam QuantityState quantityState);
}
