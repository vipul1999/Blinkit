package org.example.dto;

import lombok.Data;
import org.example.entity.Location;

@Data
public class RouteStep {
    private final String action;   // "Pickup" or "Deliver"
    private final String target;   // e.g. "Restaurant R1", "Customer C1"
    private final String orderId;  // e.g. "O1"
    private final double eta;
    private final Location location;

    // convenience
    public double getLat() { return location.getLatitude(); }
    public double getLng() { return location.getLongitude(); }

    public RouteStep(String action, String target, String orderId, double eta, Location location) {
        this.action = action;
        this.target = target;
        this.orderId = orderId;
        this.eta = eta;
        this.location = location;
    }

    @Override
    public String toString() {
        return action + " Order " + orderId + " at " + target
                + " (ETA: " + String.format("%.2f", eta) + " min)";
    }
}
