package org.example;

import org.example.entity.Location;
import org.example.entity.Order;
import org.example.utility.GeoJsonExporter;
import org.example.utility.OrderGenerator;
import org.example.service.RouteService;
import org.example.dto.RouteResult;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        try {
            Location start = new Location(12.9352, 77.6245); // Koramangala example

            // 1️⃣ Generate a batch of random orders
            List<Order> orders = generateOrders(start, 10);

            // 2️⃣ Plan the fastest route
            RouteResult fastestRoute = planRoute(start, orders);

            // 3️⃣ Export route to GeoJSON
            exportRoute(fastestRoute, Path.of("route.geojson"));

        } catch (IOException e) {
            System.err.println("Failed to export GeoJSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<Order> generateOrders(Location start, int count) {
        List<Order> orders = OrderGenerator.generateOrders(count, start);
        System.out.println("Generated Orders:");
        orders.forEach(System.out::println);
        return orders;
    }

    private static RouteResult planRoute(Location start, List<Order> orders) {
        RouteService planner = new RouteService(start, orders);
        RouteResult route = planner.findBestRoute();
        System.out.println("\nFastest Route:\n" + route);
        return route;
    }

    private static void exportRoute(RouteResult route, Path filePath) throws IOException {
        GeoJsonExporter.exportToGeoJson(route, filePath.toString());
        System.out.println("GeoJSON exported to absolute path" + filePath.toAbsolutePath());
    }
}
