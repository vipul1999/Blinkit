package org.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.example.dto.RouteResult;
import org.example.entity.Location;
import org.example.entity.Order;
import org.example.service.RouteService;
import org.example.utility.GeoJsonExporter;
import org.example.utility.OrderGenerator;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * Unit test for simple App.
 */
public class AppTest {

    @Test
    void testFullWorkflow() throws IOException {
        // 1️⃣ Setup: start location
        Location start = new Location(12.9352, 77.6245);

        // 2️⃣ Generate orders
        List<Order> orders = OrderGenerator.generateOrders(5, start);
        assertEquals(5, orders.size(), "Should generate exactly 5 orders");

        // 3️⃣ Plan the fastest route
        RouteService planner = new RouteService(start, orders);
        RouteResult result = planner.findBestRoute();

        // Basic checks on the route
        assertNotNull(result);
        assertEquals(orders.size() * 2, result.getSequence().size(), "Each order should have pickup and delivery");

        // 4️⃣ Export to temporary GeoJSON file
        File tempFile = File.createTempFile("testRoute", ".geojson");
        tempFile.deleteOnExit();

        GeoJsonExporter.exportToGeoJson(result, tempFile.getAbsolutePath());

        // Read content
        String content = Files.readString(tempFile.toPath());
        assertNotNull(content);
        assertTrue(content.contains("\"type\": \"FeatureCollection\""));
        assertTrue(content.contains("\"features\""));

        // Check that all orderIds appear in the GeoJSON
        for (Order o : orders) {
            assertTrue(content.contains(o.getOrderId()), "GeoJSON should contain orderId: " + o.getOrderId());
        }
    }
}
