package ru.practicum.feign;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.store.PageProductDto;
import ru.practicum.dto.store.ProductDto;
import ru.practicum.enums.ProductCategory;
import ru.practicum.enums.QuantityState;

import java.util.UUID;

@FeignClient(name = "shopping-store", path = "/api/v1/shopping-store")
public interface ShoppingStoreClient extends ShoppingStoreContract {
    @Override
    @GetMapping
    PageProductDto getProducts(@RequestParam ProductCategory productCategory,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(defaultValue = "productName,asc") String sort);

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
    void deleteProductById(@RequestBody UUID productId);

    @Override
    @PostMapping("/quantityState")
    Boolean updateQuantityState(@RequestParam UUID productId, @RequestParam QuantityState quantityState);
}
