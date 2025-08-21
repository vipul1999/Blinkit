package org.example.dto;

import lombok.Data;

import java.util.List;

@Data
public class RouteResult {
    public List<RouteStep> sequence;   // ✅ Now stores RouteStep
    public double totalTime;

    public RouteResult(List<RouteStep> sequence, double totalTime) {
        this.sequence = sequence;
        this.totalTime = totalTime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Best Route:\n");
        int step = 1;
        for (RouteStep s : sequence) {
            sb.append(step++).append(". ").append(s).append("\n");
        }
        sb.append("Total Time: ").append(String.format("%.2f", totalTime)).append(" minutes");
        return sb.toString();
    }
}
