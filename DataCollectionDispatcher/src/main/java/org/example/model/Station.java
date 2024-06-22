package org.example.model;

public class Station {

    private int id;
    private String dbUrl;
    private double lat;
    private double lng;

    public Station(int id, String dbUrl, double lat, double lng) {
        this.id = id;
        this.dbUrl = dbUrl;
        this.lat = lat;
        this.lng = lng;
    }

    public int getId() {
        return id;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
