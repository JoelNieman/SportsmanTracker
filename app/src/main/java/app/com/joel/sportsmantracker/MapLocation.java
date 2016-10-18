package app.com.joel.sportsmantracker;

import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Created by Joel on 10/16/16.
 */

public class MapLocation {
    private LatLng latLng;
    private String locationName;

    public MapLocation(double latitude, double longitude, String locationName) {
        this.latLng = new LatLng(latitude, longitude);
        this.locationName = locationName;
    }


    public String getLocationName() {
        return locationName;
    }

    public LatLng getLatLng() {
        return latLng;
    }
}
