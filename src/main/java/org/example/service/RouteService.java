package org.example.service;

import org.example.algo.Haversine;
import org.example.entity.Location;
import org.example.entity.Order;
import org.example.dto.RouteResult;
import org.example.dto.RouteStep;

import java.util.*;

public class RouteService {
    private final Location startLocation;
    private final List<Order> orders;

    public RouteService(Location startLocation, List<Order> orders) {
        this.startLocation = startLocation;
        this.orders = orders;
    }

    /**
     * Solve using Bitmask DP with path reconstruction.
     * Nodes: 0..n-1 = restaurants (R1..Rn), n..2n-1 = customers (C1..Cn).
     * dp[mask][pos] = minimum time to reach 'pos' having visited nodes in 'mask'.
     */
    public RouteResult findBestRoute() {
        int n = orders.size();
        if (n == 0) {
            return new RouteResult(List.of(), 0.0);  // 👈 Early return
        }
        int totalNodes = 2 * n;
        int fullMask = (1 << totalNodes) - 1;

        // Map nodes → locations
        Location[] nodeLoc = new Location[totalNodes];
        double[] prep = new double[n]; // effective prep times for restaurants

        for (int i = 0; i < n; i++) {
            nodeLoc[i] = orders.get(i).getRestaurantLocation();      // Ri
            nodeLoc[i + n] = orders.get(i).getConsumerLocation();    // Ci
            prep[i] = orders.get(i).getEffectivePrepTime();
        }

        // dp and parents
        double[][] dp = new double[1 << totalNodes][totalNodes];
        for (double[] row : dp) Arrays.fill(row, Double.MAX_VALUE);

        int[][] parentPos = new int[1 << totalNodes][totalNodes];
        int[][] parentMask = new int[1 << totalNodes][totalNodes];
        for (int[] row : parentPos) Arrays.fill(row, -1);
        for (int[] row : parentMask) Arrays.fill(row, -1);

        // Initialize from start to each restaurant (must start with a pickup)
        for (int r = 0; r < n; r++) {
            int m = (1 << r);
            double travel = Haversine.travelTime(startLocation, nodeLoc[r]);
            double arrival = Math.max(travel, prep[r]); // wait if early
            if (arrival < dp[m][r]) {
                dp[m][r] = arrival;
                parentPos[m][r] = -1;   // -1 denotes start
                parentMask[m][r] = 0;
            }
        }

        // Transitions
        for (int mask = 0; mask <= fullMask; mask++) {
            for (int pos = 0; pos < totalNodes; pos++) {
                double curT = dp[mask][pos];
                if (curT == Double.MAX_VALUE) continue;

                Location curLoc = nodeLoc[pos];

                for (int nxt = 0; nxt < totalNodes; nxt++) {
                    if ((mask & (1 << nxt)) != 0) continue; // already visited

                    // If 'nxt' is a customer j, ensure restaurant j is already visited.
                    if (nxt >= n) {
                        int j = nxt - n;
                        if ((mask & (1 << j)) == 0) continue; // cannot deliver before pickup
                    }

                    double travel = Haversine.travelTime(curLoc, nodeLoc[nxt]);
                    double arrival = curT + travel;

                    // If going to a restaurant, must wait until prep time if early
                    if (nxt < n) {
                        arrival = Math.max(arrival, prep[nxt]);
                    }

                    int newMask = mask | (1 << nxt);
                    if (arrival < dp[newMask][nxt]) {
                        dp[newMask][nxt] = arrival;
                        parentPos[newMask][nxt] = pos;
                        parentMask[newMask][nxt] = mask;
                    }
                }
            }
        }

        // Find best end state
        double bestTime = Double.MAX_VALUE;
        int bestEndPos = -1;
        for (int pos = 0; pos < totalNodes; pos++) {
            if (dp[fullMask][pos] < bestTime) {
                bestTime = dp[fullMask][pos];
                bestEndPos = pos;
            }
        }

        // Reconstruct path of node visits
        List<Integer> nodeOrder = new ArrayList<>();
        int curMask = fullMask, curPos = bestEndPos;
        while (curPos != -1) {
            nodeOrder.add(curPos);
            int pm = parentMask[curMask][curPos];
            int pp = parentPos[curMask][curPos];
            curMask = pm;
            curPos = pp;
        }
        // Add the first move from start if needed (already covered by dp init with parent -1)
        Collections.reverse(nodeOrder);

        // Convert nodes → RouteStep with ETAs from dp values
        // To get ETAs per step, simulate along the reconstructed path using the same timing logic
        List<RouteStep> steps = new ArrayList<>();
        Location cur = startLocation;
        double t = 0.0;
        boolean[] picked = new boolean[n];

        for (int node : nodeOrder) {
            boolean isRestaurant = (node < n);
            int idx = isRestaurant ? node : (node - n);

            double travel = Haversine.travelTime(cur, nodeLoc[node]);
            double arrival = t + travel;

            String action;
            String target;
            if (isRestaurant) {
                // wait for prep if early
                double eta = Math.max(arrival, prep[idx]);
                action = "Pickup";
                target = "Restaurant R" + (idx + 1);
                steps.add(new RouteStep(
                        action,
                        target,
                        orders.get(idx).getOrderId(),
                        eta,
                        nodeLoc[node]   // 👈 this is the Location of the restaurant/customer at this step
                ));
                t = eta;
                picked[idx] = true;
            } else {
                // must be already picked (guaranteed by DP)
                action = "Deliver";
                target = "Customer C" + (idx + 1);
                steps.add(new RouteStep(action, target, orders.get(idx).getOrderId(), arrival, nodeLoc[node]));
                t = arrival;
            }

            cur = nodeLoc[node];
        }

        return new RouteResult(steps, bestTime);
    }
}
