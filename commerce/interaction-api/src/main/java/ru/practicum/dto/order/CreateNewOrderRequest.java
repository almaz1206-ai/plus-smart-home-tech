package ru.practicum.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.dto.cart.ShoppingCartDto;
import ru.practicum.dto.warehouse.AddressDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateNewOrderRequest {
    @Valid
    @NotNull
    private ShoppingCartDto shoppingCart;

    @Valid
    @NotNull
    private AddressDto addressDto;
}
