package app.com.joel.sportsmantracker;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Created by Joel on 10/16/16.
 */

public class MapLocationsTest {

    private MapLocation locationOne;
    private MapLocation locationTwo;
    private sMapLocationsDAO mapLocationsDAO;

    @Before
    public void setUp() {
        locationOne = new MapLocation(42.329021, -83.03974, "Renaissance Center");
        locationTwo = new MapLocation(42.315278, -83.210278, "Ford World Headquarters");
        mapLocationsDAO = sMapLocationsDAO.getMapLocationsDAO();
    }

    @After
    public void tearDown() {
        mapLocationsDAO.clearAllLocations();
    }

    @Test
    public void constructorShouldCreateLocation() {
        assertEquals("Renaissance Center", locationOne.getLocationName());
    }

    @Test
    public void locationsShouldBeAddedToMapLocationsDAO() {
        ArrayList<MapLocation> locations = mapLocationsDAO.getLocations();
        locations.add(locationOne);
        locations.add(locationTwo);

        assertEquals(2, mapLocationsDAO.getLocations().size());
    }

    @Test
    public void allLocationsShouldBeRemoved() {
        mapLocationsDAO.addLocation(locationOne);
        mapLocationsDAO.addLocation(locationTwo);

        mapLocationsDAO.clearAllLocations();
        assertEquals(0, mapLocationsDAO.getLocations().size());
    }
}
