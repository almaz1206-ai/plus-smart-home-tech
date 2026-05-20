package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.enums.CartState;
import ru.practicum.model.ShoppingCart;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, UUID> {
    Optional<ShoppingCart> findByUsernameAndStatus(String username, CartState status);

    Optional<ShoppingCart> findByUsername(String username);
}
