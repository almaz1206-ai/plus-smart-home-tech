package ru.practicum.feign;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.store.ProductDto;
import ru.practicum.enums.ProductCategory;
import ru.practicum.enums.QuantityState;

import java.util.UUID;

@FeignClient(name = "shopping-store", path = "/api/v1/shopping-store")
public interface ShoppingStoreClient extends ShoppingStoreContract {
    @Override
    @GetMapping
    Page<ProductDto> getProducts(@RequestParam ProductCategory productCategory, Pageable pageable);

    @Override
    @GetMapping("/{productId}")
    ProductDto getProductById(@PathVariable UUID productId);

    @Override
    @PutMapping
    ProductDto createProduct(@Valid @RequestBody ProductDto productDto);

    @Override
    @PostMapping
    ProductDto updateProduct(@Valid @RequestBody ProductDto productDto);

    @Override
    @PostMapping("/removeProductFromStore")
    Boolean deleteProductById(@RequestBody UUID productId);

    @Override
    @PostMapping("/quantityState")
    Boolean updateQuantityState(@RequestParam UUID productId, @RequestParam QuantityState quantityState);
}
