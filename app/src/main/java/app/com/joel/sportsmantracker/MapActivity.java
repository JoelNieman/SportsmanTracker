package app.com.joel.sportsmantracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationListener;
import com.mapbox.mapboxsdk.location.LocationServices;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity {

    private static final int LOCATION_TITLE = 0;
    private static final int INFORMATION_ICON = 1;


    private MapView mapView;
    private MapboxMap map;
    private LocationServices locationServices;
    private sMapLocationsDAO mapLocationsDAO;
    private ArrayList<MapLocation> mapLocationsArrayList = new ArrayList<MapLocation>();
    private ActionBar actionBar;
    private int mapToDisplay = 0;
    private Location lastLocation;

    private static final int PERMISSIONS_LOCATION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        mapLocationsDAO = sMapLocationsDAO.getMapLocationsDAO();

        Resources resources = getResources();
        mapLocationsArrayList = mapLocationsDAO.loadMapLocations(resources);

        MapboxAccountManager.start(this.getApplicationContext(), "pk.eyJ1IjoibmllbWFuam8iLCJhIjoiY2l1NXZldzdlMGhzNDJ5bGtiYzVqNm44cCJ9.hBQRfDJ0PSbTuzsfdIceNQ");
        locationServices = LocationServices.getLocationServices(this.getApplicationContext());

        setContentView(R.layout.activity_map);
        setFloatingActionButtonIconColors();
        setFloatingActionButtonElevationForPreLollipop();
        setOnClickListenerForZoomButton();

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                MapActivity.this.map = mapboxMap;
                mapboxMap.setMyLocationEnabled(true);
                toggleGps(true);
                setMarkerInfoWindowAdapter();
                dropCustomPins(mapLocationsArrayList);

            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();


    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @UiThread
    public void toggleGps(boolean enableGps) {
        if (enableGps) {
            // Check if user has granted location permission
            if (!locationServices.areLocationPermissionsGranted()) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_LOCATION);
            } else {
                enableLocation(true);
            }
        } else {
            enableLocation(false);
        }
    }

    private void enableLocation(boolean enabled) {
        if (enabled) {
            locationServices.addLocationListener(new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    lastLocation = location;
//
//                    if (location != null) {
//                        // Move the map camera to where the user location is
//                        map.setCameraPosition(new CameraPosition.Builder()
//                                .target(new LatLng(location))
//                                .zoom(9)
//                                .build());
//                    }
                }
            });
            // Enable or disable the location layer on the map
            map.setMyLocationEnabled(enabled);
        } else {
            Toast.makeText(this, "Location not enabled", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_LOCATION: {
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableLocation(true);
                }
            }
        }
    }

    public void dropCustomPins(ArrayList<MapLocation> locationsForMarkers) {

        for (MapLocation location : locationsForMarkers) {
            Bitmap customMarkerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pin_custom_fishing);
            MarkerViewOptions marker = new MarkerViewOptions()
                    .position(location.getLatLng())
                    .icon(IconFactory.recreate("Marker", customMarkerBitmap))
                    .title(location.getLocationName());
            map.addMarker(marker);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        setActionBarIconColors();

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_items, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                Toast.makeText(this, "Search button selected", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_switch_layers:
                switchMapLayers();
                break;
        }
        return true;
    }

    public void switchMapLayers() {
        switch (mapToDisplay) {
            case 0:
                map.setStyleUrl("mapbox://styles/mapbox/satellite-v9");
                mapToDisplay = 1;
                break;
            case 1:
                map.setStyleUrl("mapbox://styles/mapbox/streets-v9");
                mapToDisplay = 0;
                break;
        }

    }

    public void setActionBarIconColors() {
        Drawable searchIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.icon_search, null);
        Drawable layersIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.icon_layers, null);

        ColorFilter whiteFilter = new LightingColorFilter(Color.WHITE, Color.WHITE);
        searchIcon.setColorFilter(whiteFilter);
        layersIcon.setColorFilter(whiteFilter);
    }

    public void setFloatingActionButtonIconColors() {
        Drawable setLocationIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.icon_location_set, null);
        Drawable addLocationIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.icon_location_add, null);

        ColorFilter whiteFilter = new LightingColorFilter(Color.WHITE, Color.WHITE);
        ColorFilter grayFilter = new LightingColorFilter(Color.LTGRAY, Color.LTGRAY);

        setLocationIcon.setColorFilter(grayFilter);
        addLocationIcon.setColorFilter(whiteFilter);
    }


    public void setFloatingActionButtonElevationForPreLollipop() {
        FloatingActionButton newPinButton = (FloatingActionButton) findViewById(R.id.newPin);
        FloatingActionButton zoomButton = (FloatingActionButton) findViewById(R.id.zoomButton);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {

            zoomButton.setCompatElevation(0);
            newPinButton.setCompatElevation(0);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) newPinButton.getLayoutParams();
            params.setMargins(0, 0, 16, 16); // get rid of margins since shadow area is now the margin
            newPinButton.setLayoutParams(params);
            zoomButton.setLayoutParams(params);
        }
    }


    public void setOnClickListenerForZoomButton() {
        FloatingActionButton zoomButton = (FloatingActionButton) findViewById(R.id.zoomButton);

        zoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (lastLocation != null) {
                    zoomToCurrentLocation(lastLocation);
                }
            }
        });
    }

    private void zoomToCurrentLocation(Location location) {
        if (location != null) {
            // Move the map camera to where the user location is
            map.setCameraPosition(new CameraPosition.Builder()
                    .target(new LatLng(lastLocation))
                    .zoom(9)
                    .build());
            }
        }





    public void setMarkerInfoWindowAdapter() {

        map.setInfoWindowAdapter(new MapboxMap.InfoWindowAdapter() {
            @Nullable
            @Override
            public View getInfoWindow(@NonNull Marker marker) {

                LinearLayout parentLayout = new LinearLayout(MapActivity.this);
                parentLayout.setLayoutParams(new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                parentLayout.setBackgroundColor(Color.WHITE);
                parentLayout.setGravity(Gravity.BOTTOM);

                TextView titleText = new TextView(MapActivity.this);
                LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                titleText.setLayoutParams(titleParams);
                titleText.setText(marker.getTitle());
                titleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                titleText.setTextColor(Color.BLACK);


                ImageView infoIcon = new ImageView(MapActivity.this);
                infoIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_info_outline_black_24dp, null));

                float iconLeftMarginFloat = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics());
                int iconLeftMarginInt = (int) iconLeftMarginFloat;

                float iconMaxHeightFloat = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()) + 8;
                int iconMaxHeightAndWidthInt = (int) iconMaxHeightFloat;

                LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                        iconMaxHeightAndWidthInt, iconMaxHeightAndWidthInt);

                infoIcon.setLayoutParams(iconParams);
                iconParams.setMargins(iconLeftMarginInt,0,0,0);

                ColorFilter darkBlueFilter = new LightingColorFilter(Color.argb(1, 2,107,158), Color.argb(1, 2,107,158));
                infoIcon.setColorFilter(darkBlueFilter);

                parentLayout.addView(titleText);
                parentLayout.addView(infoIcon);
                parentLayout.setPadding(20, 20, 20, 20);

                return parentLayout;
            }
        });
    }
}


