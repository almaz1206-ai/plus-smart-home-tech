package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.store.ProductDto;
import ru.practicum.enums.ProductCategory;
import ru.practicum.enums.ProductState;
import ru.practicum.enums.QuantityState;
import ru.practicum.exception.ProductNotFoundException;
import ru.practicum.mapper.ProductMapper;
import ru.practicum.model.Product;
import ru.practicum.repository.ProductRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> getProducts(ProductCategory productCategory, Pageable pageable) {
        Page<Product> products = productRepository.findAllByProductCategory(productCategory, pageable);

        List<ProductDto> productDtos = products.stream().map(mapper::toProductDto).toList();
        return new PageImpl<>(productDtos, pageable, products.getTotalElements());
    }

    @Override
    public ProductDto createProduct(ProductDto productDto) {
        Product product = productRepository.save(mapper.toProduct(productDto));

        return mapper.toProductDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProductById(UUID productId) {
        Product product = getExistingProduct(productId);

        return mapper.toProductDto(product);
    }

    @Override
    @Transactional
    public ProductDto updateProduct(ProductDto productDto) {
        log.info("Обновление продукта с id: {}", productDto.getProductId());
        Product existingProduct = getExistingProduct(productDto.getProductId());

        mapper.updateFromDto(productDto, existingProduct);

        Product updated = productRepository.save(existingProduct);

        log.info("Продукт обновлен: {}", updated.getProductId());
        return mapper.toProductDto(updated);
    }

    @Override
    @Transactional
    public boolean deleteProductById(UUID productId) {
        log.info("Удаление продукта с id: {}", productId);
        if (!productRepository.existsById(productId)) {
            return false;
        }

        Product product = getExistingProduct(productId);
        product.setProductState(ProductState.DEACTIVATE);

        productRepository.save(product);
        return true;
    }

    @Override
    public boolean updateQuantityState(UUID productId, QuantityState quantityState) {
        Product product = getExistingProduct(productId);
        product.setQuantityState(quantityState);
        productRepository.save(product);

        return true;
    }

    private Product getExistingProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(String.format("Продукт с id %s не найден", productId)));
    }
}
