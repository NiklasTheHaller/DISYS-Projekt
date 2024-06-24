package org.example.model;

import java.util.List;

public class AggregatedData {

    private int customerId;
    private List<Charge> charges;
    private long startTime;  // Added startTime field
    private long totalTime;  // Added totalTime field

    public AggregatedData(int customerId, List<Charge> charges, long startTime, long totalTime) {
        this.customerId = customerId;
        this.charges = charges;
        this.startTime = startTime;
        this.totalTime = totalTime;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public List<Charge> getCharges() {
        return charges;
    }

    public void setCharges(List<Charge> charges) {
        this.charges = charges;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }
}
