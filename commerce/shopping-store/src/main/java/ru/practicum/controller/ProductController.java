package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.store.PageProductDto;
import ru.practicum.dto.store.PageableObject;
import ru.practicum.dto.store.ProductDto;
import ru.practicum.enums.ProductCategory;
import ru.practicum.enums.QuantityState;
import ru.practicum.feign.ShoppingStoreContract;
import ru.practicum.service.ProductService;
import ru.practicum.utils.PageableUtils;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shopping-store")
public class ProductController implements ShoppingStoreContract {
    private final ProductService productService;


    @Override
    public PageProductDto getProducts(@RequestParam ProductCategory category,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size,
                                      @RequestParam(defaultValue = "productName,asc") String sort) {

        String[] sortArray = new String[]{sort};

        PageableObject pageable = PageableUtils.createPageableObject(page, size, sortArray);
        return productService.getProducts(category, pageable);
    }

    @Override
    public ProductDto getProductById(@PathVariable UUID productId) {
        return productService.getProductById(productId);
    }

    @Override
    public ProductDto createProduct(@Valid @RequestBody ProductDto productDto) {
        return productService.createProduct(productDto);
    }

    @Override
    public ProductDto updateProduct(@Valid @RequestBody ProductDto productDto) {
        return productService.updateProduct(productDto);
    }

    @Override
    public void deleteProductById(@RequestBody UUID productId) {
        productService.deleteProductById(productId);
    }

    @Override
    public Boolean updateQuantityState(@RequestParam UUID productId,
                                       @RequestParam QuantityState quantityState) {
        return productService.updateQuantityState(productId, quantityState);
    }
}
