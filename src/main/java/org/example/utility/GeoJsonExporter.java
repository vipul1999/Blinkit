package org.example.utility;

import org.example.dto.RouteResult;
import org.example.dto.RouteStep;

import java.io.FileWriter;
import java.io.IOException;

public class GeoJsonExporter {

    public static void exportToGeoJson(RouteResult result, String filePath) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"type\": \"FeatureCollection\",\n  \"features\": [\n");

        // Full route LineString
        sb.append("    {\n")
                .append("      \"type\": \"Feature\",\n")
                .append("      \"geometry\": { \"type\": \"LineString\", \"coordinates\": [\n");
        for (int i = 0; i < result.sequence.size(); i++) {
            RouteStep step = result.sequence.get(i);
            sb.append("        [").append(step.getLng()).append(", ").append(step.getLat()).append("]");
            if (i < result.sequence.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("      ]},\n")
                .append("      \"properties\": { \"name\": \"Delivery Route\" }\n")
                .append("    }");

        // Arrows for direction (small segments)
        for (int i = 0; i < result.sequence.size() - 1; i++) {
            RouteStep from = result.sequence.get(i);
            RouteStep to = result.sequence.get(i + 1);

            sb.append(",\n    {\n")
                    .append("      \"type\": \"Feature\",\n")
                    .append("      \"geometry\": { \"type\": \"LineString\", \"coordinates\": [\n")
                    .append("        [").append(from.getLng()).append(", ").append(from.getLat()).append("],\n")
                    .append("        [").append(to.getLng()).append(", ").append(to.getLat()).append("]\n")
                    .append("      ]},\n")
                    .append("      \"properties\": { \"arrow\": true }\n")
                    .append("    }");
        }

        // Point of origin (first location in the sequence, yellow marker)
        if (!result.sequence.isEmpty()) {
            RouteStep origin = result.sequence.get(0);
            sb.append(",\n    {\n")
                    .append("      \"type\": \"Feature\",\n")
                    .append("      \"geometry\": { \"type\": \"Point\", \"coordinates\": [")
                    .append(origin.getLng()).append(", ").append(origin.getLat()).append("] },\n")
                    .append("      \"properties\": {\n")
                    .append("        \"marker-color\": \"yellow\",\n")
                    .append("        \"type\": \"origin\"\n")
                    .append("      }\n")
                    .append("    }");
        }

        // Other point features (markers for pickups and deliveries)
        for (int i = 0; i < result.sequence.size(); i++) {
            RouteStep step = result.sequence.get(i);

            // Skip the origin marker (first point already marked)
            if (i == 0) continue;

            // Decide marker color
            String markerColor = "gray";
            if ("Pickup".equalsIgnoreCase(step.getAction())) {
                markerColor = "red";
            } else if ("Deliver".equalsIgnoreCase(step.getAction())) {
                markerColor = "green";
            }

            sb.append(",\n    {\n")
                    .append("      \"type\": \"Feature\",\n")
                    .append("      \"geometry\": { \"type\": \"Point\", \"coordinates\": [")
                    .append(step.getLng()).append(", ").append(step.getLat()).append("] },\n")
                    .append("      \"properties\": {\n")
                    .append("        \"action\": \"").append(step.getAction()).append("\",\n")
                    .append("        \"target\": \"").append(step.getTarget()).append("\",\n")
                    .append("        \"orderId\": \"").append(step.getOrderId()).append("\",\n")
                    .append("        \"marker-color\": \"").append(markerColor).append("\",\n")
                    .append("        \"eta\": ").append(String.format("%.2f", step.getEta())).append("\n")
                    .append("      }\n")
                    .append("    }");
        }

        sb.append("\n  ]\n}\n");

        try (FileWriter w = new FileWriter(filePath)) {
            w.write(sb.toString());
        }
    }
}
