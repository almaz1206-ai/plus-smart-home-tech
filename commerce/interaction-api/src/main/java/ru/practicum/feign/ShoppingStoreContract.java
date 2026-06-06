package ru.practicum.feign;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.store.PageProductDto;
import ru.practicum.dto.store.ProductDto;
import ru.practicum.enums.ProductCategory;
import ru.practicum.enums.QuantityState;

import java.util.UUID;


public interface ShoppingStoreContract {

    @GetMapping
    PageProductDto getProducts(@RequestParam ProductCategory productCategory,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(defaultValue = "productName,asc") String sort);

    @GetMapping("/{productId}")
    ProductDto getProductById(@PathVariable UUID productId);

    @PutMapping
    ProductDto createProduct(@Valid @RequestBody ProductDto productDto);

    @PostMapping
    ProductDto updateProduct(@Valid @RequestBody ProductDto productDto);

    @DeleteMapping
    void deleteProductById(@RequestBody UUID productId);

    @PostMapping("/quantityState")
    Boolean updateQuantityState(@RequestParam UUID productId, @RequestParam QuantityState quantityState);

}
