package ru.practicum.feign;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.store.ChangeProductQuantityDto;
import ru.practicum.dto.cart.ShoppingCartDto;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface ShoppingCartContract {
    ShoppingCartDto getShoppingCart(@RequestParam String userName);

    ShoppingCartDto addProductToCart(@RequestParam String username, @RequestBody @NotNull Map<UUID, Long> products);

    void deactivateCart(@RequestParam String userName);

    ShoppingCartDto deleteProduct(@RequestParam String username, @RequestBody Set<UUID> products);

    ShoppingCartDto updateProductQuantity(@RequestParam String username,
                                          @RequestBody @Valid ChangeProductQuantityDto request);
}
