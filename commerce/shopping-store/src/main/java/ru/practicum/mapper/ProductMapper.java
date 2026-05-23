package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.store.ProductDto;
import ru.practicum.model.Product;

@Component
public class ProductMapper {
    public Product toProduct(ProductDto productDto) {
        return Product.builder()
                .productId(productDto.getProductId())
                .productCategory(productDto.getProductCategory())
                .productState(productDto.getProductState())
                .productName(productDto.getProductName())
                .description(productDto.getDescription())
                .price(productDto.getPrice())
                .imageSrc(productDto.getImageSrc())
                .quantityState(productDto.getQuantityState())
                .build();
    }

    public ProductDto toProductDto(Product product) {
        return ProductDto.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .description(product.getDescription())
                .imageSrc(product.getImageSrc())
                .quantityState(product.getQuantityState())
                .productState(product.getProductState())
                .productCategory(product.getProductCategory())
                .price(product.getPrice())
                .build();
    }

    public void updateFromDto(ProductDto dto, Product product) {
        if (dto.getProductName() != null) {
            product.setProductName(dto.getProductName());
        }
        if (dto.getDescription() != null) {
            product.setDescription(dto.getDescription());
        }
        if (dto.getImageSrc() != null) {
            product.setImageSrc(dto.getImageSrc());
        }
        if (dto.getPrice() != null) {
            product.setPrice(dto.getPrice());
        }
        if (dto.getProductCategory() != null) {
            product.setProductCategory(dto.getProductCategory());
        }
        if (dto.getProductState() != null) {
            product.setProductState(dto.getProductState());
        }
        if (dto.getQuantityState() != null) {
            product.setQuantityState(dto.getQuantityState());
        }
    }
}
