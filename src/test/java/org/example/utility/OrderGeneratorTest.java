package org.example.utility;

import org.example.entity.Location;
import org.example.entity.Order;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderGeneratorTest {

    @Test
    void testGenerateOrdersSize() {
        Location base = new Location(0, 0);
        List<Order> orders = OrderGenerator.generateOrders(5, base);

        // Check that we get exactly 5 orders
        assertEquals(5, orders.size(), "Should generate exactly 5 orders");
    }

    @Test
    void testGenerateOrdersFields1() {
        Location base = new Location(10, 10);
        List<Order> orders = OrderGenerator.generateOrders(10, base);

        for (int i = 0; i < orders.size(); i++) {
            Order o = orders.get(i);

            // Check order IDs
            assertEquals("O" + (i + 1), o.getOrderId());

            // Restaurant location within ~1km (~0.01 lat/lon tolerance)
            assertTrue(Math.abs(o.getRestaurantLocation().getLatitude() - base.getLatitude()) <= 0.01);
            assertTrue(Math.abs(o.getRestaurantLocation().getLongitude() - base.getLongitude()) <= 0.01);

            // Consumer location within ~2km (~0.02 lat/lon tolerance)
            assertTrue(Math.abs(o.getConsumerLocation().getLatitude() - base.getLatitude()) <= 0.02);
            assertTrue(Math.abs(o.getConsumerLocation().getLongitude() - base.getLongitude()) <= 0.02);

            assertTrue(o.getEffectivePrepTime() >= o.getPrepTime(),
                    "Effective prep time should be >= prep time");
            assertTrue(o.getEffectivePrepTime() <= o.getPrepTime() + 3,
                    "Effective prep time should not exceed prepTime + 3");

            // Trust buffer between 0 and 3
            assertTrue(o.getTrustBuffer() >= 0 && o.getTrustBuffer() <= 3);
        }
    }


    @Test
    void testGenerateZeroOrders() {
        Location base = new Location(0, 0);
        List<Order> orders = OrderGenerator.generateOrders(0, base);

        // Should return an empty list
        assertNotNull(orders);
        assertTrue(orders.isEmpty(), "Generating 0 orders should return empty list");
    }

}