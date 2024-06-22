package org.example.model;

public class Charge {

    private int id;
    private float kwh;
    private int customerId;

    public Charge(int id, float kwh, int customerId) {
        this.id = id;
        this.kwh = kwh;
        this.customerId = customerId;
    }

    public int getId() {
        return id;
    }

    public float getKwh() {
        return kwh;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setKwh(float kwh) {
        this.kwh = kwh;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }
}
