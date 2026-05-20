package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.store.ProductDto;
import ru.practicum.enums.ProductCategory;
import ru.practicum.enums.QuantityState;
import ru.practicum.feign.ShoppingStoreContract;
import ru.practicum.service.ProductService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shopping-store")
public class ProductController implements ShoppingStoreContract {
    private final ProductService productService;

    @Override
    @GetMapping
    public Page<ProductDto> getProducts(@RequestParam ProductCategory category,
                                        Pageable pageable) {
        return productService.getProducts(category, pageable);
    }

    @Override
    @GetMapping("/{productId}")
    public ProductDto getProductById(@PathVariable UUID productId) {
        return productService.getProductById(productId);
    }

    @Override
    @PutMapping
    public ProductDto createProduct(@Valid @RequestBody ProductDto productDto) {
        return productService.createProduct(productDto);
    }

    @Override
    @PostMapping
    public ProductDto updateProduct(@Valid @RequestBody ProductDto productDto) {
        return productService.updateProduct(productDto);
    }

    @Override
    @PostMapping("/removeProductFromStore")
    public Boolean deleteProductById(@RequestBody UUID productId) {
        return productService.deleteProductById(productId);
    }

    @Override
    @PostMapping("/quantityState")
    public Boolean updateQuantityState(@RequestParam UUID productId,
                                       @RequestParam QuantityState quantityState) {
        return productService.updateQuantityState(productId, quantityState);
    }
}
