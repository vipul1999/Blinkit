package org.example.utility;

import org.example.entity.Location;
import org.example.entity.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OrderGenerator {
    private static final Random random = new Random();

    public static List<Order> generateOrders(int n, Location base) {
        List<Order> orders = new ArrayList<>();

        for (int i = 1; i <= n; i++) {
            // Randomize restaurant within ~1km of base
            double restLat = base.getLatitude() + (random.nextDouble() - 0.5) * 0.02;
            double restLon = base.getLongitude() + (random.nextDouble() - 0.5) * 0.02;

            // Randomize consumer within ~2km of base
            double custLat = base.getLatitude() + (random.nextDouble() - 0.5) * 0.04;
            double custLon = base.getLongitude() + (random.nextDouble() - 0.5) * 0.04;

            // Prep time between 3 and 10 minutes
            double prepTime = 3 + random.nextInt(8);

            // Trust factor buffer (say 0–3 minutes)
            double trustBuffer = random.nextInt(4);

            orders.add(new Order(
                    "O" + i,
                    new Location(custLat, custLon),
                    new Location(restLat, restLon),
                    prepTime,
                    trustBuffer
            ));
        }
        return orders;
    }
}
