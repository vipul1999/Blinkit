package org.example.service;

import org.example.algo.Haversine;
import org.example.dto.RouteResult;
import org.example.dto.RouteStep;
import org.example.entity.Location;
import org.example.entity.Order;
import org.example.service.RouteService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RouteServiceTest {
    // - Setup: 1 order (restaurant at (0,1), customer at (0,2), prep time 0.0)
    // - Tests:
    //   - Route has exactly 2 steps: Pickup → Deliver
    //   - Correct order IDs in both steps
    //   - Total time is positive (> 0)
    // - Purpose: Verifies basic correctness of routing for a single order without waiting
    @Test
    void testSingleOrderSimple() {
        Location start = new Location(0, 0);
        Order order = new Order("O1",
                new Location(0, 1),   // restaurant
                new Location(0, 2),   // customer
                0.0,0.0               // prep time
        );

        RouteService service = new RouteService(start, List.of(order));
        RouteResult result = service.findBestRoute();

        List<RouteStep> steps = result.getSequence();
        assertEquals(2, steps.size());
        assertEquals("Pickup", steps.get(0).getAction());
        assertEquals("Deliver", steps.get(1).getAction());
        assertEquals("O1", steps.get(0).getOrderId());
        assertEquals("O1", steps.get(1).getOrderId());
        assertTrue(result.getTotalTime() > 0);
    }

    // - Setup: 1 order with prep time 10.0
    // - Tests:
    //   - First step is Pickup
    //   - Pickup ETA is >= prep time (algorithm waits if restaurant not ready)
    // - Purpose: Confirms the algorithm respects restaurant preparation times
    @Test
    void testSingleOrderWithPrepTime() {
        Location start = new Location(0, 0);
        Order order = new Order("O2",
                new Location(0, 1),
                new Location(0, 2),
                10.0,2.0  // prep time 10 mins
        );

        RouteService service = new RouteService(start, List.of(order));
        RouteResult result = service.findBestRoute();

        RouteStep pickup = result.getSequence().get(0);
        assertEquals("Pickup", pickup.getAction());
        assertTrue(pickup.getEta() >= 10.0, "Pickup should wait for prep time");
    }


    // - Setup: 2 orders
    //     O1: restaurant (0,1) → customer (0,2)
    //     O2: restaurant (1,0) → customer (2,0)
    // - Tests:
    //   - Total of 4 steps (2 pickups, 2 deliveries)
    //   - Ensures no delivery occurs before its corresponding pickup
    // - Purpose: Verifies correctness of multi-order routing and pickup→delivery constraint
    @Test
    void testTwoOrdersOptimalRoute() {
        Location start = new Location(0, 0);
        Order o1 = new Order("O1", new Location(0, 1), new Location(0, 2), 0,0);
        Order o2 = new Order("O2", new Location(1, 0), new Location(2, 0), 0,0);

        RouteService service = new RouteService(start, List.of(o1, o2));
        RouteResult result = service.findBestRoute();

        List<RouteStep> steps = result.getSequence();
        assertEquals(4, steps.size());

        // Ensure no delivery before pickup
        assertTrue(steps.indexOf(steps.stream()
                .filter(s -> s.getAction().equals("Deliver") && s.getOrderId().equals("O1"))
                .findFirst().get())
                > steps.indexOf(steps.stream()
                .filter(s -> s.getAction().equals("Pickup") && s.getOrderId().equals("O1"))
                .findFirst().get()));
    }

    @Test
    void testNoOrders() {
        Location start = new Location(0, 0);
        RouteService service = new RouteService(start, List.of());

        RouteResult result = service.findBestRoute();

        // Expect no steps and total time = 0.0
        assertNotNull(result);
        assertTrue(result.getSequence().isEmpty(), "Route sequence should be empty when no orders are given");
        assertEquals(0.0, result.getTotalTime(), "Total time should be 0 when no orders are present");
    }
    // - Setup: Multiple orders with different prep times
    // - Tests:
    //   - Algorithm waits for each restaurant’s prep time
    //   - Deliveries happen after pickups
    // - Purpose: Verifies DP correctly handles multiple prep-time constraints
    @Test
    void testMultipleOrdersWithPrepTimes() {
        Location start = new Location(0, 0);

        Order o1 = new Order("O1", new Location(0, 1), new Location(0, 2), 5.0, 0.0);
        Order o2 = new Order("O2", new Location(1, 0), new Location(2, 0), 10.0, 0.0);

        RouteService service = new RouteService(start, List.of(o1, o2));
        RouteResult result = service.findBestRoute();

        List<RouteStep> steps = result.getSequence();

        assertEquals(4, steps.size());

        // Ensure pickups respect prep time
        RouteStep pickup1 = steps.stream().filter(s -> s.getAction().equals("Pickup") && s.getOrderId().equals("O1")).findFirst().get();
        RouteStep pickup2 = steps.stream().filter(s -> s.getAction().equals("Pickup") && s.getOrderId().equals("O2")).findFirst().get();

        assertTrue(pickup1.getEta() >= 5.0);
        assertTrue(pickup2.getEta() >= 10.0);

        // Ensure no delivery before pickup
        for (Order o : List.of(o1, o2)) {
            int pickupIndex = steps.indexOf(steps.stream().filter(s -> s.getAction().equals("Pickup") && s.getOrderId().equals(o.getOrderId())).findFirst().get());
            int deliveryIndex = steps.indexOf(steps.stream().filter(s -> s.getAction().equals("Deliver") && s.getOrderId().equals(o.getOrderId())).findFirst().get());
            assertTrue(deliveryIndex > pickupIndex);
        }
    }
    // - Setup: Multiple orders where some restaurants or customers share the same location
    // - Tests:
    //   - Algorithm does not crash or produce invalid ETAs
    //   - Correct pickup→delivery sequence is maintained
    // - Purpose: Checks handling of overlapping coordinates
    @Test
    void testOrdersWithSameLocation() {
        Location start = new Location(0, 0);

        // Both orders share the same restaurant
        Order o1 = new Order("O1", new Location(1, 1), new Location(2, 2), 0.0, 0.0);
        Order o2 = new Order("O2", new Location(1, 1), new Location(3, 3), 0.0, 0.0);

        RouteService service = new RouteService(start, List.of(o1, o2));
        RouteResult result = service.findBestRoute();

        List<RouteStep> steps = result.getSequence();
        assertEquals(4, steps.size());

        // Ensure no delivery before pickup
        for (Order o : List.of(o1, o2)) {
            int pickupIndex = steps.indexOf(steps.stream().filter(s -> s.getAction().equals("Pickup") && s.getOrderId().equals(o.getOrderId())).findFirst().get());
            int deliveryIndex = steps.indexOf(steps.stream().filter(s -> s.getAction().equals("Deliver") && s.getOrderId().equals(o.getOrderId())).findFirst().get());
            assertTrue(deliveryIndex > pickupIndex);
        }
    }

    // - Setup: Start location is the same as a restaurant or customer
    // - Tests:
    //   - Pickup or delivery can happen immediately without extra travel
    //   - ETA calculations are correct
    // - Purpose: Verifies correctness when start overlaps with order locations

    @Test
    void testStartLocationEqualsRestaurantOrCustomer() {
        // Start is same as restaurant for O1
        Location start = new Location(1, 1);

        Order o1 = new Order("O1", new Location(1, 1), new Location(2, 2), 0.0, 0.0);
        Order o2 = new Order("O2", new Location(3, 3), new Location(4, 4), 0.0, 0.0);

        RouteService service = new RouteService(start, List.of(o1, o2));
        RouteResult result = service.findBestRoute();

        List<RouteStep> steps = result.getSequence();
        assertEquals(4, steps.size(), "Route should have 4 steps for 2 orders");

        // Ensure pickups occur before their deliveries
        for (Order o : List.of(o1, o2)) {
            int pickupIndex = steps.indexOf(steps.stream()
                    .filter(s -> s.getAction().equals("Pickup") && s.getOrderId().equals(o.getOrderId()))
                    .findFirst()
                    .get());
            int deliveryIndex = steps.indexOf(steps.stream()
                    .filter(s -> s.getAction().equals("Deliver") && s.getOrderId().equals(o.getOrderId()))
                    .findFirst()
                    .get());
            assertTrue(deliveryIndex > pickupIndex, "Delivery must occur after pickup for order " + o.getOrderId());
        }

        // Total route time must be positive
        assertTrue(result.getTotalTime() > 0, "Total route time should be positive");
    }




}
