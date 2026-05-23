package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.store.PageProductDto;
import ru.practicum.dto.store.PageableObject;
import ru.practicum.dto.store.ProductDto;
import ru.practicum.dto.store.SortObject;
import ru.practicum.enums.ProductCategory;
import ru.practicum.enums.ProductState;
import ru.practicum.enums.QuantityState;
import ru.practicum.exception.ProductNotFoundException;
import ru.practicum.mapper.ProductMapper;
import ru.practicum.model.Product;
import ru.practicum.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PageProductDto getProducts(ProductCategory productCategory, PageableObject pageable) {
        log.info("Получение продуктов с категорией: {}, pageable: {}", productCategory, pageable);

        try {
            Pageable springPageable = convertToSpringPageable(pageable);
            Page<Product> products = productRepository.findAllByProductCategory(productCategory, springPageable);


            return convertToPageProductDto(products);
        } catch (Exception e) {
            log.error("Ошибка получения продуктов: ", e);
            throw new RuntimeException("Ошибка при получении продуктов: " + e.getMessage(), e);
        }

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
    public void deleteProductById(UUID productId) {
        log.info("Удаление продукта с id: {}", productId);

        Product product = getExistingProduct(productId);
        product.setProductState(ProductState.DEACTIVATE);

        productRepository.save(product);
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

    private Pageable convertToSpringPageable(PageableObject pageable) {
        if (pageable == null) {
            return PageRequest.of(0, 10);
        }

        Sort sort = createSort(pageable.getSort());

        int pageNumber = pageable.getPageNumber() != null ? pageable.getPageNumber() : 0;
        int pageSize = pageable.getPageSize() != null ? pageable.getPageSize() : 10;

        return PageRequest.of(
                pageNumber,
                pageSize,
                sort
        );
    }

    private Sort createSort(SortObject sortObj) {
        if (sortObj == null || sortObj.getProperty() == null) {
            return Sort.unsorted();
        }

        String directionString = sortObj.getDirection().trim();

        Sort.Direction direction = "DESC".equalsIgnoreCase(directionString)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return Sort.by(direction, sortObj.getProperty());
    }

    private PageProductDto convertToPageProductDto(Page<Product> page) {
        return PageProductDto.builder()
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .size(page.getSize())
                .number(page.getNumber())
                .numberOfElements(page.getNumberOfElements())
                .empty(page.isEmpty())
                .content(convertContent(page.getContent()))
                .sort(convertSort(page.getSort()))
                .pageable(convertPageable(page.getPageable()))
                .build();
    }

    private List<ProductDto> convertContent(List<Product> products) {
        return products.stream()
                .map(product -> {
                    try {
                        return mapper.toProductDto(product);
                    } catch (Exception e) {
                        log.error("ошибка конвертации продукта: {}", product.getProductId(), e);
                        return null;
                    }
                })
                .collect(Collectors.toList());
    }

    private List<SortObject> convertSort(Sort sort) {
        List<SortObject> sortObjects = new ArrayList<>();

        for (Sort.Order order : sort) {
            SortObject sortObj = SortObject.builder()
                    .direction(order.getDirection().name())
                    .ascending(order.isAscending())
                    .property(order.getProperty())
                    .ignoreCase(order.isIgnoreCase())
                    .nullHanding(order.getNullHandling().name())
                    .build();
            sortObjects.add(sortObj);
        }

        return sortObjects;
    }

    private PageableObject convertPageable(Pageable pageable) {
        PageableObject pageableDto =PageableObject.builder()
                .offset(pageable.getOffset())
                .pageNumber(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .paged(pageable.isPaged())
                .unpaged(pageable.isUnpaged())
                .build();

        if (pageable.getSort().isSorted()) {
            Sort.Order firstOrder = pageable.getSort().iterator().next();
            SortObject sortObj = SortObject.builder()
                    .direction(firstOrder.getDirection().name())
                    .ascending(firstOrder.isAscending())
                    .property(firstOrder.getProperty())
                    .ignoreCase(firstOrder.isIgnoreCase())
                    .build();
            pageableDto.setSort(sortObj);
        }

        return pageableDto;
    }
}
