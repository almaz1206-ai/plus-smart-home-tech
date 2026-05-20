package ru.practicum.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.cart.ShoppingCartDto;
import ru.practicum.dto.store.ChangeProductQuantityDto;
import ru.practicum.dto.warehouse.BookedProductsDto;
import ru.practicum.enums.CartState;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.NotAuthorizedUserException;
import ru.practicum.feign.WarehouseClient;
import ru.practicum.mapper.ShoppingCartMapper;
import ru.practicum.model.ShoppingCart;
import ru.practicum.repository.ShoppingCartRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final ShoppingCartRepository repository;
    private final ShoppingCartMapper mapper;
    private final WarehouseClient warehouseClient;

    @Override
    @Transactional(readOnly = true)
    public ShoppingCartDto getShoppingCart(String username) {
        checkUsername(username);
        log.debug("Поиска корзины пользователя: {}", username);
        ShoppingCart cart = repository.findByUsername(username)
                .orElseGet(() -> ShoppingCart.builder()
                        .username(username)
                        .status(CartState.ACTIVE)
                        .products(new HashMap<>())
                        .build());

        return mapper.toShoppingCartDto(cart);
    }

    @Override
    @Transactional
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Long> products) {
        checkUsername(username);
        log.info("Запрос на добавление товаров в корзину пользователя: {}, количество товаров: {}",
                username, products.size());

        if (products.isEmpty()) {
            throw new BadRequestException("Список продуктов при добавлении не может быть пустым");
        }
        ShoppingCart cart = repository.findByUsername(username)
                .orElseGet(() -> ShoppingCart.builder()
                        .username(username)
                        .status(CartState.ACTIVE)
                        .products(new HashMap<>())
                        .build());

        cart = repository.save(cart);

        updateCartProducts(cart, products);

        try {
            log.info("Проверка наличия на складе: id={}, products={}", cart.getShoppingCartId(), cart.getProducts());
            BookedProductsDto bookedProductsDto = warehouseClient.checkProductQuantityInWarehouse(mapper.toShoppingCartDto(cart));

            log.info("Проверено на складе: {}", bookedProductsDto);
        } catch (FeignException e) {
            throw new RuntimeException("Склад недоступен");
        }

        cart = repository.save(cart);

        ShoppingCartDto res = mapper.toShoppingCartDto(cart);

        log.info("Товары добавлены в корзину пользователя: {}, итоговое количество товаров: {}", username,
                res.getProducts().size());

        return res;
    }

    @Override
    public void deactivateCurrentShoppingCart(String username) {
        checkUsername(username);
        log.info("Запрос на деактивацию корзины для пользователя={}", username);
        ShoppingCart cart = repository.findByUsernameAndStatus(username, CartState.ACTIVE)
                .orElseThrow(() -> new BadRequestException(String.format(
                        "Активная корзина для пользователя %s не найдена", username)));

        cart.setStatus(CartState.DEACTIVATE);
        repository.save(cart);
        log.info("Корзина пользователя {} успешно деактивирована", username);
    }

    @Override
    public ShoppingCartDto deleteProductFromShoppingCart(String username, Set<UUID> request) {
        checkUsername(username);

        if (request == null || request.isEmpty()) {
            throw new BadRequestException("Список товаров для удаления не может быть пустым");
        }

        ShoppingCart cart = repository.findByUsernameAndStatus(username, CartState.ACTIVE)
                .orElseThrow(() -> new BadRequestException(String.format(
                        "Активная корзина для пользователя %s не найдена", username)));

        if (cart.getProducts() != null) {
            cart.getProducts().keySet().removeAll(request);
        }

        return mapper.toShoppingCartDto(repository.save(cart));
    }

    @Override
    public ShoppingCartDto updateProductQuantity(String username, ChangeProductQuantityDto requestDto) {
        checkUsername(username);

        if (requestDto == null) {
            throw new BadRequestException("Запрос на обновлениеи не может быть null");
        }

        UUID productId = requestDto.getProductId();
        Long newQuantity = requestDto.getNewQuantity();

        if (productId == null) {
            throw new BadRequestException("productId не может быть пустым");
        }

        log.info("Обновление количества товара для пользователя={}, товар={}, новое количество={}",
                username, productId, newQuantity);

        ShoppingCart cart = repository.findByUsernameAndStatus(username, CartState.ACTIVE)
                .orElseThrow(() -> new BadRequestException(String.format(
                        "Не найдена активная корзина для пользователя: %s", username)));

        if (cart.getProducts() == null) {
            cart.setProducts(new HashMap<>());
        }

        if (newQuantity == 0) {
            cart.getProducts().remove(productId);
        } else {
            cart.getProducts().put(productId, newQuantity);
        }

        return mapper.toShoppingCartDto(repository.save(cart));
    }

    private void checkUsername(String username) {
        if (username == null || username.isEmpty()) {
            throw new NotAuthorizedUserException("Имя пользователя не должно быть пустым");
        }
    }

    private void updateCartProducts(ShoppingCart cart, Map<UUID, Long> newProducts) {
        if (cart.getProducts() == null) {
            cart.setProducts(new HashMap<>());
        }

        newProducts.forEach((productId, quantity) -> cart.getProducts().merge(productId, quantity, Long::sum));
    }
}
