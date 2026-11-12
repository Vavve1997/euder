package com.vavedin.euder.feature.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Address(
        @Column(name = "street", nullable = false, length = 200)
        String street,

        @Column(name = "house_number", nullable = false, length = 20)
        String houseNumber,

        @Column(name = "postal_code", nullable = false, length = 20)
        String postalCode,

        @Column(name = "city", nullable = false, length = 100)
        String city
) {
}
