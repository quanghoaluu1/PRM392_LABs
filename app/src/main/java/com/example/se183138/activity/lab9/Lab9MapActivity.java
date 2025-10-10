package com.example.se183138.activity.lab9;

import static androidx.core.location.LocationManagerCompat.getCurrentLocation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.se183138.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.maplibre.android.MapLibre;
import org.maplibre.android.WellKnownTileServer;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.geometry.LatLngBounds;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.OnMapReadyCallback;
import org.maplibre.android.maps.Style;
import org.maplibre.android.style.layers.LineLayer;
import org.maplibre.android.style.layers.Property;
import org.maplibre.android.style.layers.PropertyFactory;
import org.maplibre.android.style.layers.SymbolLayer;
import org.maplibre.android.style.sources.GeoJsonSource;
import org.maplibre.geojson.Feature;
import org.maplibre.geojson.FeatureCollection;
import org.maplibre.geojson.LineString;
import org.maplibre.geojson.Point;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Lab9MapActivity extends AppCompatActivity {
    private MapView mapView;
    private MapLibreMap mapLibreMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private FloatingActionButton btnRecenter;
    private LatLng currentLatLng;
    private LatLng destinationLatLng;
    
    // UI Elements
    private EditText etSearchAddress;
    private ImageView ivClearSearch;
    private CardView suggestionsCard;
    private RecyclerView rvSuggestions;
    private SuggestionsAdapter suggestionsAdapter;
    
    // Autocomplete
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final long SEARCH_DELAY_MS = 100; // 500ms debounce
    private static final String OPENMAP_API_KEY = "RnHDS87LT9D7tvArA8MeWEivuq7L5OQl";
    
    // Map constants
    private static final String MARKER_SOURCE_ID = "marker-source";
    private static final String MARKER_LAYER_ID = "marker-layer";
    private static final String RED_MARKER_ICON_ID = "red-marker-icon";
    private static final String BLUE_MARKER_ICON_ID = "blue-marker-icon";
    private static final String DEST_MARKER_SOURCE_ID = "dest-marker-source";
    private static final String DEST_MARKER_LAYER_ID = "dest-marker-layer";
    private static final String ROUTE_SOURCE_ID = "route-source";
    private static final String ROUTE_LAYER_ID = "route-layer";
    
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        MapLibre.getInstance(
                this,
                "no-api-key",  // OpenMap.vn không yêu cầu key
                WellKnownTileServer.MapTiler // hoặc WellKnownTileServer.None
        );

        setContentView(R.layout.lab9_map);

        // Initialize views
        mapView = findViewById(R.id.mapView);
        btnRecenter = findViewById(R.id.btnRecenter);
        etSearchAddress = findViewById(R.id.etSearchAddress);
        ivClearSearch = findViewById(R.id.ivClearSearch);
        suggestionsCard = findViewById(R.id.suggestionsCard);
        rvSuggestions = findViewById(R.id.rvSuggestions);
        
        mapView.onCreate(savedInstanceState);
        executorService = Executors.newSingleThreadExecutor();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Setup RecyclerView
        suggestionsAdapter = new SuggestionsAdapter(this::onSuggestionSelected);
        rvSuggestions.setLayoutManager(new LinearLayoutManager(this));
        rvSuggestions.setAdapter(suggestionsAdapter);

        // Setup search functionality
        setupSearchBar();

        mapView.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(@NonNull MapLibreMap maplibreMap) {
                mapLibreMap = maplibreMap;
                maplibreMap.setStyle(new Style.Builder()
                        .fromUri("https://maptiles.openmap.vn/styles/night-v1/style.json?apikey=RnHDS87LT9D7tvArA8MeWEivuq7L5OQl"),
                        style -> getCurrentLocation()
                );
            }
        });

        btnRecenter.setOnClickListener(v -> getCurrentLocation());
    }
    
    private void setupSearchBar() {
        // Show/hide clear button and autocomplete based on text
        etSearchAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ivClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                
                // Cancel previous search
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                
                // Trigger autocomplete with debounce
                if (s.length() > 2) { // Only search if 3+ characters
                    searchRunnable = () -> fetchAutocompleteSuggestions(s.toString());
                    searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
                } else {
                    suggestionsCard.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Clear button click
        ivClearSearch.setOnClickListener(v -> {
            etSearchAddress.setText("");
            suggestionsCard.setVisibility(View.GONE);
            clearRoute();
        });
        
        // Handle search action
        etSearchAddress.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || 
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                suggestionsCard.setVisibility(View.GONE);
                searchAndNavigate(etSearchAddress.getText().toString());
                return true;
            }
            return false;
        });
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null && mapLibreMap != null){
                    currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(currentLatLng)
                            .zoom(15)
                            .build();
                    mapLibreMap.animateCamera(
                            org.maplibre.android.camera.CameraUpdateFactory.newCameraPosition(cameraPosition), 2000
                    );
                    mapLibreMap.getLocationComponent().activateLocationComponent(
                            org.maplibre.android.location.LocationComponentActivationOptions
                                    .builder(Lab9MapActivity.this, mapLibreMap.getStyle())
                                    .build()
                    );
                    mapLibreMap.getLocationComponent().setLocationComponentEnabled(true);
                    
                    // Add or update marker using modern Style API
                    addMarkerToMap(currentLatLng);
                }
            }
        });
    }

    private void addMarkerToMap(LatLng latLng) {
        Style style = mapLibreMap.getStyle();
        if (style == null) {
            return;
        }
        
        // Add the red marker icon to the map style if not already added
        if (style.getImage(RED_MARKER_ICON_ID) == null) {
            Bitmap markerBitmap = getBitmapFromVectorDrawable(R.drawable.red_marker);
            if (markerBitmap != null) {
                style.addImage(RED_MARKER_ICON_ID, markerBitmap);
            }
        }
        
        // Create a GeoJSON point feature for the marker
        Point point = Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude());
        Feature feature = Feature.fromGeometry(point);
        
        // Update or create the marker source
        GeoJsonSource source = style.getSourceAs(MARKER_SOURCE_ID);
        if (source != null) {
            // Update existing source
            source.setGeoJson(feature);
        } else {
            // Create new source and layer
            source = new GeoJsonSource(MARKER_SOURCE_ID, feature);
            style.addSource(source);
            
            SymbolLayer symbolLayer = new SymbolLayer(MARKER_LAYER_ID, MARKER_SOURCE_ID)
                    .withProperties(
                            PropertyFactory.iconImage(RED_MARKER_ICON_ID),
                            PropertyFactory.iconSize(0.15f),  // Scale down to 15% of original size
                            PropertyFactory.iconAllowOverlap(true),
                            PropertyFactory.iconIgnorePlacement(true)
                    );
            style.addLayer(symbolLayer);
        }
    }
    
    private Bitmap getBitmapFromVectorDrawable(int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(this, drawableId);
        if (drawable == null) {
            return null;
        }
        
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
    
    private void fetchAutocompleteSuggestions(String query) {
        executorService.execute(() -> {
            try {
                String location = "";
                if (currentLatLng != null) {
                    location = "&location=" + currentLatLng.getLatitude() + "," + currentLatLng.getLongitude();
                }
                
                String urlString = String.format(Locale.US,
                        "https://mapapis.openmap.vn/v1/autocomplete?input=%s%s&radius=50&apikey=%s",
                        java.net.URLEncoder.encode(query, "UTF-8"),
                        location,
                        OPENMAP_API_KEY);
                
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                conn.disconnect();
                
                // Parse JSON response
                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.getString("status").equals("OK")) {
                    JSONArray predictions = jsonResponse.getJSONArray("predictions");
                    List<PlacePrediction> results = new ArrayList<>();
                    
                    for (int i = 0; i < predictions.length(); i++) {
                        JSONObject prediction = predictions.getJSONObject(i);
                        String description = prediction.getString("description");
                        String placeId = prediction.getString("place_id");
                        
                        String mainText = description;
                        String secondaryText = "";
                        
                        if (prediction.has("structured_formatting")) {
                            JSONObject formatting = prediction.getJSONObject("structured_formatting");
                            mainText = formatting.getString("main_text");
                            secondaryText = formatting.getString("secondary_text");
                        }
                        
                        results.add(new PlacePrediction(description, placeId, mainText, secondaryText));
                    }
                    
                    runOnUiThread(() -> {
                        if (!results.isEmpty()) {
                            suggestionsAdapter.updateSuggestions(results);
                            suggestionsCard.setVisibility(View.VISIBLE);
                        } else {
                            suggestionsCard.setVisibility(View.GONE);
                        }
                    });
                } else {
                    runOnUiThread(() -> suggestionsCard.setVisibility(View.GONE));
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> suggestionsCard.setVisibility(View.GONE));
            }
        });
    }
    
    private void onSuggestionSelected(PlacePrediction prediction) {
        // Hide suggestions
        suggestionsCard.setVisibility(View.GONE);
        
        // Set text to search bar
        etSearchAddress.setText(prediction.getMainText());
        
        // Navigate using the full description
        searchAndNavigate(prediction.getDescription());
    }
    
    private void searchAndNavigate(String address) {
        if (address.trim().isEmpty()) {
            Toast.makeText(this, "Please enter an address", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (currentLatLng == null) {
            Toast.makeText(this, "Current location not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Geocode the address in background
        executorService.execute(() -> {
            try {
                Geocoder geocoder = new Geocoder(Lab9MapActivity.this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocationName(address, 1);
                
                if (addresses != null && !addresses.isEmpty()) {
                    Address location = addresses.get(0);
                    destinationLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    
                    runOnUiThread(() -> {
                        Toast.makeText(Lab9MapActivity.this, "Found: " + location.getAddressLine(0), Toast.LENGTH_SHORT).show();
                        // Add destination marker
                        addDestinationMarker(destinationLatLng);
                        // Get route
                        getRoute(currentLatLng, destinationLatLng);
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(Lab9MapActivity.this, "Address not found", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(Lab9MapActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }
    
    private void addDestinationMarker(LatLng latLng) {
        Style style = mapLibreMap.getStyle();
        if (style == null) {
            return;
        }
        
        // Add the blue marker icon to the map style if not already added
        if (style.getImage(BLUE_MARKER_ICON_ID) == null) {
            Bitmap markerBitmap = getBitmapFromVectorDrawable(R.drawable.blue_marker);
            if (markerBitmap != null) {
                style.addImage(BLUE_MARKER_ICON_ID, markerBitmap);
            }
        }
        
        // Create a GeoJSON point feature for the destination marker
        Point point = Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude());
        Feature feature = Feature.fromGeometry(point);
        
        // Update or create the destination marker source
        GeoJsonSource source = style.getSourceAs(DEST_MARKER_SOURCE_ID);
        if (source != null) {
            source.setGeoJson(feature);
        } else {
            source = new GeoJsonSource(DEST_MARKER_SOURCE_ID, feature);
            style.addSource(source);
            
            // Use blue marker icon for destination
            SymbolLayer symbolLayer = new SymbolLayer(DEST_MARKER_LAYER_ID, DEST_MARKER_SOURCE_ID)
                    .withProperties(
                            PropertyFactory.iconImage(BLUE_MARKER_ICON_ID),
                            PropertyFactory.iconSize(0.15f),
                            PropertyFactory.iconAllowOverlap(true),
                            PropertyFactory.iconIgnorePlacement(true)
                    );
            style.addLayer(symbolLayer);
        }
    }
    
    private void getRoute(LatLng start, LatLng end) {
        executorService.execute(() -> {
            try {
                // Using OSRM (Open Source Routing Machine) - free routing API
                String urlString = String.format(Locale.US,
                        "https://router.project-osrm.org/route/v1/driving/%f,%f;%f,%f?overview=full&geometries=geojson",
                        start.getLongitude(), start.getLatitude(),
                        end.getLongitude(), end.getLatitude());
                
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                conn.disconnect();
                
                // Parse JSON response
                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.getString("code").equals("Ok")) {
                    JSONArray routes = jsonResponse.getJSONArray("routes");
                    if (routes.length() > 0) {
                        JSONObject route = routes.getJSONObject(0);
                        JSONObject geometry = route.getJSONObject("geometry");
                        JSONArray coordinates = geometry.getJSONArray("coordinates");
                        
                        // Convert coordinates to LatLng list
                        List<Point> points = new ArrayList<>();
                        for (int i = 0; i < coordinates.length(); i++) {
                            JSONArray coord = coordinates.getJSONArray(i);
                            points.add(Point.fromLngLat(coord.getDouble(0), coord.getDouble(1)));
                        }
                        
                        // Get distance and duration
                        double distance = route.getDouble("distance") / 1000; // Convert to km
                        double duration = route.getDouble("duration") / 60; // Convert to minutes
                        
                        runOnUiThread(() -> {
                            drawRoute(points);
                            fitBoundsToRoute(start, end);
                            Toast.makeText(Lab9MapActivity.this, 
                                    String.format(Locale.US, "Distance: %.1f km, Time: %.0f min", distance, duration), 
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(Lab9MapActivity.this, "Route not found", Toast.LENGTH_SHORT).show());
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(Lab9MapActivity.this, "Error getting route: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }
    
    private void drawRoute(List<Point> points) {
        Style style = mapLibreMap.getStyle();
        if (style == null) {
            return;
        }
        
        // Create LineString from points
        LineString lineString = LineString.fromLngLats(points);
        
        // Update or create route source
        GeoJsonSource source = style.getSourceAs(ROUTE_SOURCE_ID);
        if (source != null) {
            source.setGeoJson(lineString);
        } else {
            source = new GeoJsonSource(ROUTE_SOURCE_ID, lineString);
            style.addSource(source);
            
            // Create line layer with Google Maps-like styling
            LineLayer lineLayer = new LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID)
                    .withProperties(
                            PropertyFactory.lineColor(Color.parseColor("#4285F4")), // Google blue
                            PropertyFactory.lineWidth(6f),
                            PropertyFactory.lineOpacity(0.9f),
                            PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                            PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND)
                    );
            style.addLayer(lineLayer);
        }
    }
    
    private void fitBoundsToRoute(LatLng start, LatLng end) {
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boundsBuilder.include(start);
        boundsBuilder.include(end);
        LatLngBounds bounds = boundsBuilder.build();
        
        mapLibreMap.animateCamera(
                org.maplibre.android.camera.CameraUpdateFactory.newLatLngBounds(bounds, 100),
                2000
        );
    }
    
    private void clearRoute() {
        Style style = mapLibreMap.getStyle();
        if (style == null) {
            return;
        }
        
        // Clear route line
        GeoJsonSource routeSource = style.getSourceAs(ROUTE_SOURCE_ID);
        if (routeSource != null) {
            routeSource.setGeoJson(FeatureCollection.fromFeatures(new ArrayList<>()));
        }
        
        // Clear destination marker
        GeoJsonSource destSource = style.getSourceAs(DEST_MARKER_SOURCE_ID);
        if (destSource != null) {
            destSource.setGeoJson(FeatureCollection.fromFeatures(new ArrayList<>()));
        }
        
        destinationLatLng = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }
    }

    @Override
    protected void onStart() { super.onStart(); mapView.onStart(); }
    @Override
    protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override
    protected void onPause() { super.onPause(); mapView.onPause(); }
    @Override
    protected void onStop() { super.onStop(); mapView.onStop(); }
    @Override
    public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
    @Override
    protected void onDestroy() { 
        super.onDestroy(); 
        mapView.onDestroy(); 
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
