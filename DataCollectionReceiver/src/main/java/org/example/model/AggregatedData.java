package org.example.model;

import java.util.List;

public class AggregatedData {

    private int customerId;
    private List<Charge> charges;

    public AggregatedData(int customerId, List<Charge> charges) {
        this.customerId = customerId;
        this.charges = charges;
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
}
