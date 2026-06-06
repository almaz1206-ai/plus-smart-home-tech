package ru.practicum.model;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address {
    private String country;
    private String city;
    private String street;
    private String house;
    private String flat;
}
