package ru.practicum.service;

import ru.practicum.dto.cart.ShoppingCartDto;
import ru.practicum.dto.store.ChangeProductQuantityDto;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface ShoppingCartService {
    ShoppingCartDto getShoppingCart(String userName);

    ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Long> products);

    void deactivateCurrentShoppingCart(String username);

    ShoppingCartDto deleteProductFromShoppingCart(String username, Set<UUID> request);

    ShoppingCartDto updateProductQuantity(String username, ChangeProductQuantityDto requestDto);
}
