package com.openshift.evg.roadshow.rest.gateway.model;

/**
 * This represents a backend. Once a backend is registered, a call to the
 * backend to get this information about it will be issued.
 *
 * Created by jmorales on 24/08/16.
 */
public class Backend {

    public static final String BACKEND_TYPE_MARKER  = "marker";
    public static final String BACKEND_TYPE_CLUSTER = "cluster";
    public static final String BACKEND_TYPE_TEMP    = "temp";
    public static final String BACKEND_TYPE_HEATMAP = "heatmap";

    public static final String BACKEND_SCOPE_ALL   = "all";
    public static final String BACKEND_SCOPE_WITHIN = "within";

    private String id;
    private String displayName;
    private Coordinates center = new Coordinates("0", "0");
    private int zoom = 1;
    private int maxZoom = 1;
    private String type = BACKEND_TYPE_CLUSTER;
    private boolean visible = true;
    private String scope = BACKEND_SCOPE_ALL;

    public Backend() {
    }

    public Backend(String id, String displayName, String service) {
        this.id = id;
        this.displayName = displayName;
    }

    public Backend(String id, String displayName, Coordinates center, int zoom) {
        this.id = id;
        this.displayName = displayName;
        this.center = center;
        this.zoom = zoom;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Coordinates getCenter() {
        return center;
    }

    public void setCenter(Coordinates center) {
        this.center = center;
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    public int getMaxZoom() {
        return maxZoom;
    }

    public void setMaxZoom(int maxzoom) {
        this.maxZoom = maxzoom;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    public String toString() {
        return "Backend{" +
                "id='" + id + '\'' +
                ", displayName='" + displayName + '\'' +
                ", center='" + center + '\'' +
                ", zoom='" + zoom + '\'' +
                ", type='" + type + '\'' +
                ", scope='" + scope + '\'' +
                ", visible='" + visible + '\'' +
                ", maxZoom='" + maxZoom + '\'' +
                '}';
    }

}
