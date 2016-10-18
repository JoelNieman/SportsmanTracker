package app.com.joel.sportsmantracker;

import android.content.res.Resources;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Joel on 10/16/16.
 */

public class sMapLocationsDAO {

    private static sMapLocationsDAO mapLocationsDAO;
    private ArrayList<MapLocation> mapLocationsArray = new ArrayList<MapLocation>();



    private sMapLocationsDAO() {
    }

    public static sMapLocationsDAO getMapLocationsDAO() {
        if (mapLocationsDAO != null) {
            return mapLocationsDAO;
        } else {
            mapLocationsDAO = new sMapLocationsDAO();
            return mapLocationsDAO;
        }
    }


    public void addLocation(MapLocation locationToAdd) {
        mapLocationsArray.add(locationToAdd);
    }

    public ArrayList<MapLocation> getLocations() {
        return this.mapLocationsArray;
    }

    public void clearAllLocations() {
        this.mapLocationsArray.clear();
    }

    private String loadJSONFromFile(Resources resources) {
        String json = null;
        try {
            InputStream is = resources.openRawResource(R.raw.map_locations);
//            InputStream is = assetManager.open("res/raw/map_locations.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");

        } catch (IOException ex){
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private ArrayList<MapLocation> parseJSONString(String jsonString) {
        ArrayList<MapLocation> parsedLocations = new ArrayList<MapLocation>();

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray mapLocations = jsonObject.getJSONArray("MapLocations");

            for (int i = 0; i < mapLocations.length(); i++) {
                JSONObject mapLocationJSONObject = (JSONObject) mapLocations.get(i);
                double lat = mapLocationJSONObject.getDouble("Lat");
                double lng = mapLocationJSONObject.getDouble("Lng");
                String name = mapLocationJSONObject.getString("name");

                MapLocation location = new MapLocation(lat, lng, name);
                parsedLocations.add(location);
            }

        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
        return parsedLocations;
    }

    public ArrayList<MapLocation> loadMapLocations(Resources resources) {
        String jsonString = loadJSONFromFile(resources);
        ArrayList<MapLocation> locations = parseJSONString(jsonString);
        mapLocationsArray.addAll(locations);
        return mapLocationsArray;

    }
}
