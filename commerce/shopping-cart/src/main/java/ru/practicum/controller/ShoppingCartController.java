package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.cart.ShoppingCartDto;
import ru.practicum.dto.store.ChangeProductQuantityDto;
import ru.practicum.feign.ShoppingCartContract;
import ru.practicum.service.ShoppingCartService;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shopping-cart")
public class ShoppingCartController implements ShoppingCartContract {
    private final ShoppingCartService shoppingCartService;

    @Override
    @GetMapping
    public ShoppingCartDto getShoppingCart(@RequestParam String username) {
        return shoppingCartService.getShoppingCart(username);
    }

    @Override
    @PutMapping
    public ShoppingCartDto addProductToCart(@RequestParam String username, @RequestBody Map<UUID, Long> products) {
        return shoppingCartService.addProductToShoppingCart(username, products);
    }

    @Override
    @DeleteMapping
    public void deactivateCart(@RequestParam String username) {
        shoppingCartService.deactivateCurrentShoppingCart(username);
    }

    @Override
    @PostMapping("/remove")
    public ShoppingCartDto deleteProduct(@RequestParam String username, @RequestBody Set<UUID> request) {
        return shoppingCartService.deleteProductFromShoppingCart(username, request);
    }

    @Override
    @PostMapping("/change-quantity")
    public ShoppingCartDto updateProductQuantity(@RequestParam String username,
                                                 @RequestBody @Valid ChangeProductQuantityDto request) {
        return shoppingCartService.updateProductQuantity(username, request);
    }
}
