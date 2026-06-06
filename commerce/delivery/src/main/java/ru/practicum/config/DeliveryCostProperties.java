package ru.practicum.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@Getter
@Setter
@ConfigurationProperties(prefix = "delivery.cost")
public class DeliveryCostProperties {
    private BigDecimal baseCost;
    private BigDecimal address2Multiplier;
    private BigDecimal fragileRate;
    private BigDecimal weightRate;
    private BigDecimal volumeRate;
    private BigDecimal differentStreetRate;
}
