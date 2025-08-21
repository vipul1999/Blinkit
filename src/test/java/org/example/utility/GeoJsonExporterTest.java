package org.example.utility;

import org.example.dto.RouteResult;
import org.example.dto.RouteStep;
import org.example.entity.Location;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeoJsonExporterTest {

    @Test
    void testExportToGeoJson() throws IOException {
        // Setup RouteResult with a few steps
        RouteStep step1 = new RouteStep("Pickup", "Restaurant R1", "O1", 0.0, new Location(10.0, 20.0));
        RouteStep step2 = new RouteStep("Deliver", "Customer C1", "O1", 5.0, new Location(11.0, 21.0));
        RouteResult result = new RouteResult(List.of(step1, step2), 5.0);

        // Temporary file
        File tempFile = File.createTempFile("route", ".geojson");
        tempFile.deleteOnExit();

        // Call exporter
        GeoJsonExporter.exportToGeoJson(result, tempFile.getAbsolutePath());

        // Read file content
        String content = Files.readString(tempFile.toPath());

        // Basic assertions
        assertNotNull(content);
        assertTrue(content.contains("\"type\": \"FeatureCollection\""), "Should contain FeatureCollection");
        assertTrue(content.contains("\"type\": \"LineString\""), "Should contain LineString for route");
        assertTrue(content.contains("\"type\": \"Point\""), "Should contain Point features");
        assertTrue(content.contains("\"action\": \"Pickup\""), "Should contain Pickup action");
        assertTrue(content.contains("\"action\": \"Deliver\""), "Should contain Deliver action");
        assertTrue(content.contains("\"orderId\": \"O1\""), "Should contain correct orderId");

        assertTrue(content.contains("[20.0, 10.0]"));
        assertTrue(content.contains("[21.0, 11.0]"));
    }

    @Test
    void testExportEmptyRoute() throws IOException, IOException {
        RouteResult empty = new RouteResult(List.of(), 0.0);

        File tempFile = File.createTempFile("emptyRoute", ".geojson");
        tempFile.deleteOnExit();

        GeoJsonExporter.exportToGeoJson(empty, tempFile.getAbsolutePath());

        String content = Files.readString(tempFile.toPath());

        // It should still generate a valid JSON with empty features

        assertTrue(content.contains("\"coordinates\": [\n      ]") || content.contains("\"coordinates\": []"));
        assertTrue(content.contains("\"type\": \"FeatureCollection\""), "GeoJSON should be a FeatureCollection");
        assertTrue(content.contains("\"features\""), "GeoJSON should contain features array");
    }

}