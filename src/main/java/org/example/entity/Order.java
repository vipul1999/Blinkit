package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.entity.Location;

@Data
public class Order {
    private final String orderId;            // <-- NEW
    private final Location consumerLocation;
    private final Location restaurantLocation;
    private final double prepTime;
    private final double trustBuffer;

    public double getEffectivePrepTime() {
        return prepTime + trustBuffer;
    }
}

