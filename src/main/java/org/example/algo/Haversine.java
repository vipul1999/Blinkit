package org.example.algo;

import org.example.entity.Location;

public class Haversine {
    private static final double EARTH_RADIUS = 6371; // in km
    private static final double SPEED = 20.0; // km/h (given)

    public static double distance(Location l1, Location l2) {
        double lat1 = Math.toRadians(l1.getLatitude());
        double lon1 = Math.toRadians(l1.getLongitude());
        double lat2 = Math.toRadians(l2.getLatitude());
        double lon2 = Math.toRadians(l2.getLongitude());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.pow(Math.sin(dLon / 2), 2);

        double c = 2 * Math.asin(Math.sqrt(a));
        return EARTH_RADIUS * c;
    }

    public static double travelTime(Location l1, Location l2) {
        return (distance(l1, l2) / SPEED) * 60.0; // in minutes
    }
}
