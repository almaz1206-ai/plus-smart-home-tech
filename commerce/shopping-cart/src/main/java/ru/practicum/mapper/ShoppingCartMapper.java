package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.cart.ShoppingCartDto;
import ru.practicum.model.ShoppingCart;

@Component
public class ShoppingCartMapper {
    public ShoppingCartDto toShoppingCartDto(ShoppingCart shoppingCart) {
        return new ShoppingCartDto(shoppingCart.getShoppingCartId(), shoppingCart.getProducts(), shoppingCart.getUsername());
    }
}
