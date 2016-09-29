package com.openshift.evg.roadshow.rest.gateway.model;

public class DataPoint {

    private String id;
    private String name;

    private Coordinates position;

    private String longitude;
    private String latitude;

    private String info;

    public DataPoint() {
    }

    public DataPoint(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Object getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Coordinates getPosition() {
        return position;
    }

    public void setPosition(Coordinates position) {
        this.position = position;
    }

    public Object getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public Object getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return "NationalPark{" +
                "id=" + id +
                ", name=" + name +
                ", coordinates=" + position +
                ", info=" + info +
                '}';
    }
}
