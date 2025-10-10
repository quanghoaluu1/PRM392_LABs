package com.example.se183138.activity.lab9;

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
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.se183138.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.maplibre.android.MapLibre;
import org.maplibre.android.WellKnownTileServer;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.geometry.LatLngBounds;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
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
    private LatLng departureLatLng;
    private LatLng destinationLatLng;
    
    // UI Elements
    private EditText etDeparture;
    private EditText etDestination;
    private ImageView ivClearDeparture;
    private ImageView ivClearDestination;
    private ImageView btnSwapLocations;
    private TextView tvDepartureCurrentLoc;
    private TextView tvDestinationCurrentLoc;
    private CardView suggestionsCard;
    private CardView distanceCard;
    private RecyclerView rvSuggestions;
    private SuggestionsAdapter suggestionsAdapter;
    private TextView tvDistance;
    private TextView tvDuration;
    
    // Tracking which field is active
    private EditText activeEditText = null;
    private boolean isDepartureUsingCurrentLocation = false;
    private boolean isDestinationUsingCurrentLocation = false;
    
    // Autocomplete
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final long SEARCH_DELAY_MS = 100;
    private static final String OPENMAP_API_KEY = "RnHDS87LT9D7tvArA8MeWEivuq7L5OQl";
    
    // Map constants
    private static final String DEPARTURE_MARKER_SOURCE_ID = "departure-marker-source";
    private static final String DEPARTURE_MARKER_LAYER_ID = "departure-marker-layer";
    private static final String GREEN_MARKER_ICON_ID = "green-marker-icon";
    private static final String RED_MARKER_ICON_ID = "red-marker-icon";
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
                "no-api-key",
                WellKnownTileServer.MapTiler
        );

        setContentView(R.layout.lab9_map);

        // Initialize views
        mapView = findViewById(R.id.mapView);
        btnRecenter = findViewById(R.id.btnRecenter);
        etDeparture = findViewById(R.id.etDeparture);
        etDestination = findViewById(R.id.etDestination);
        ivClearDeparture = findViewById(R.id.ivClearDeparture);
        ivClearDestination = findViewById(R.id.ivClearDestination);
        btnSwapLocations = findViewById(R.id.btnSwapLocations);
        tvDepartureCurrentLoc = findViewById(R.id.tvDepartureCurrentLoc);
        tvDestinationCurrentLoc = findViewById(R.id.tvDestinationCurrentLoc);
        suggestionsCard = findViewById(R.id.suggestionsCard);
        distanceCard = findViewById(R.id.distanceCard);
        rvSuggestions = findViewById(R.id.rvSuggestions);
        tvDistance = findViewById(R.id.tvDistance);
        tvDuration = findViewById(R.id.tvDuration);
        
        mapView.onCreate(savedInstanceState);
        executorService = Executors.newSingleThreadExecutor();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Setup RecyclerView
        suggestionsAdapter = new SuggestionsAdapter(this::onSuggestionSelected);
        rvSuggestions.setLayoutManager(new LinearLayoutManager(this));
        rvSuggestions.setAdapter(suggestionsAdapter);

        // Setup search functionality
        setupInputFields();

        mapView.getMapAsync(maplibreMap -> {
            mapLibreMap = maplibreMap;
            mapLibreMap.setStyle(new Style.Builder()
                    .fromUri("https://maptiles.openmap.vn/styles/night-v1/style.json?apikey=RnHDS87LT9D7tvArA8MeWEivuq7L5OQl"),
                    style -> {
                        // Get current location and set as departure when ready
                        getCurrentLocation(true);
                    }
            );
        });

        btnRecenter.setOnClickListener(v -> {
            if (departureLatLng != null && destinationLatLng != null) {
                fitBoundsToRoute(departureLatLng, destinationLatLng);
            } else if (currentLatLng != null) {
                recenterToCurrentLocation();
            } else {
                getCurrentLocation();
            }
        });
        
        btnSwapLocations.setOnClickListener(v -> swapLocations());
    }
    
    private void setupInputFields() {
        // Departure field
        etDeparture.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ivClearDeparture.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                
                if (isDepartureUsingCurrentLocation && s.length() > 0) {
                    isDepartureUsingCurrentLocation = false;
                    updateCurrentLocationOptions();
                }
                
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                
                // Only show suggestions if the field has focus and user is typing
                if (s.length() > 2 && etDeparture.hasFocus()) {
                    activeEditText = etDeparture;
                    searchRunnable = () -> fetchAutocompleteSuggestions(s.toString());
                    searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
                } else {
                    suggestionsCard.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        etDeparture.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // Hide suggestions when focus is lost
                suggestionsCard.setVisibility(View.GONE);
            }
        });
        
        etDeparture.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                suggestionsCard.setVisibility(View.GONE);
                etDestination.requestFocus();
                return true;
            }
            return false;
        });
        
        ivClearDeparture.setOnClickListener(v -> {
            etDeparture.setText("");
            departureLatLng = null;
            isDepartureUsingCurrentLocation = false;
            updateCurrentLocationOptions();
            clearRoute();
            suggestionsCard.setVisibility(View.GONE);
        });
        
        tvDepartureCurrentLoc.setOnClickListener(v -> {
            if (!isDepartureUsingCurrentLocation) {
                setDepartureToCurrentLocation();
            }
            suggestionsCard.setVisibility(View.GONE);
            hideKeyboard();
        });
        
        // Destination field
        etDestination.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ivClearDestination.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                
                if (isDestinationUsingCurrentLocation && s.length() > 0) {
                    isDestinationUsingCurrentLocation = false;
                    updateCurrentLocationOptions();
                }
                
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                
                // Only show suggestions if the field has focus and user is typing
                if (s.length() > 2 && etDestination.hasFocus()) {
                    activeEditText = etDestination;
                    searchRunnable = () -> fetchAutocompleteSuggestions(s.toString());
                    searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
                } else {
                    suggestionsCard.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        etDestination.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // Hide suggestions when focus is lost
                suggestionsCard.setVisibility(View.GONE);
            }
        });
        
        etDestination.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                suggestionsCard.setVisibility(View.GONE);
                hideKeyboard();
                calculateRoute();
                return true;
            }
            return false;
        });
        
        ivClearDestination.setOnClickListener(v -> {
            etDestination.setText("");
            destinationLatLng = null;
            isDestinationUsingCurrentLocation = false;
            updateCurrentLocationOptions();
            clearRoute();
            suggestionsCard.setVisibility(View.GONE);
        });
        
        tvDestinationCurrentLoc.setOnClickListener(v -> {
            if (!isDestinationUsingCurrentLocation) {
                setDestinationToCurrentLocation();
            }
            suggestionsCard.setVisibility(View.GONE);
            hideKeyboard();
        });
    }
    
    private void setDepartureToCurrentLocation() {
        if (currentLatLng == null) {
            Toast.makeText(this, "Getting current location...", Toast.LENGTH_SHORT).show();
            getCurrentLocation(true); // Will set as departure once location is retrieved
            return;
        }
        
        etDeparture.setText("Current Location");
        departureLatLng = currentLatLng;
        isDepartureUsingCurrentLocation = true;
        updateCurrentLocationOptions();
        addDepartureMarker(departureLatLng);
        
        // Auto-calculate route if destination is set
        if (destinationLatLng != null) {
            calculateRoute();
        }
    }
    
    private void setDestinationToCurrentLocation() {
        if (currentLatLng == null) {
            Toast.makeText(this, "Getting current location...", Toast.LENGTH_SHORT).show();
            getCurrentLocation(false); // Just get location, user can click again to set as destination
            return;
        }
        
        etDestination.setText("Current Location");
        destinationLatLng = currentLatLng;
        isDestinationUsingCurrentLocation = true;
        updateCurrentLocationOptions();
        addDestinationMarker(destinationLatLng);
        
        // Auto-calculate route if departure is set
        if (departureLatLng != null) {
            calculateRoute();
        }
    }
    
    private void updateCurrentLocationOptions() {
        // Disable current location option if already used in one field
        if (isDepartureUsingCurrentLocation) {
            tvDestinationCurrentLoc.setVisibility(View.GONE);
            tvDepartureCurrentLoc.setVisibility(View.VISIBLE);
        } else if (isDestinationUsingCurrentLocation) {
            tvDepartureCurrentLoc.setVisibility(View.GONE);
            tvDestinationCurrentLoc.setVisibility(View.VISIBLE);
        } else {
            tvDepartureCurrentLoc.setVisibility(View.VISIBLE);
            tvDestinationCurrentLoc.setVisibility(View.VISIBLE);
        }
    }
    
    private void swapLocations() {
        // Swap text
        String tempText = etDeparture.getText().toString();
        etDeparture.setText(etDestination.getText().toString());
        etDestination.setText(tempText);
        
        // Swap coordinates
        LatLng tempLatLng = departureLatLng;
        departureLatLng = destinationLatLng;
        destinationLatLng = tempLatLng;
        
        // Swap current location flags
        boolean tempFlag = isDepartureUsingCurrentLocation;
        isDepartureUsingCurrentLocation = isDestinationUsingCurrentLocation;
        isDestinationUsingCurrentLocation = tempFlag;
        
        // Update UI
        updateCurrentLocationOptions();
        
        // Update markers
        if (departureLatLng != null) {
            addDepartureMarker(departureLatLng);
        }
        if (destinationLatLng != null) {
            addDestinationMarker(destinationLatLng);
        }
        
        // Recalculate route if both locations exist
        if (departureLatLng != null && destinationLatLng != null) {
            calculateRoute();
        } else {
            clearRoute();
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        getCurrentLocation(false);
    }
    
    @SuppressLint("MissingPermission")
    private void getCurrentLocation(boolean setAsDeparture) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null && mapLibreMap != null){
                currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                recenterToCurrentLocation();
                
                mapLibreMap.getLocationComponent().activateLocationComponent(
                        org.maplibre.android.location.LocationComponentActivationOptions
                                .builder(Lab9MapActivity.this, mapLibreMap.getStyle())
                                .build()
                );
                mapLibreMap.getLocationComponent().setLocationComponentEnabled(true);
                
                // Set as departure if requested (on app start)
                if (setAsDeparture) {
                    setDepartureToCurrentLocation();
                }
            } else {
                Toast.makeText(Lab9MapActivity.this, "Unable to get current location. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(Lab9MapActivity.this, "Error getting location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
    
    private void recenterToCurrentLocation() {
        if (currentLatLng != null && mapLibreMap != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(currentLatLng)
                    .zoom(15)
                    .build();
            mapLibreMap.animateCamera(
                    org.maplibre.android.camera.CameraUpdateFactory.newCameraPosition(cameraPosition), 2000
            );
        }
    }

    private void addDepartureMarker(LatLng latLng) {
        Style style = mapLibreMap.getStyle();
        if (style == null) {
            return;
        }
        
        // Add the green marker icon
        if (style.getImage(GREEN_MARKER_ICON_ID) == null) {
            Bitmap markerBitmap = getBitmapFromVectorDrawable(R.drawable.red_marker);
            if (markerBitmap != null) {
                // Tint to green
                style.addImage(GREEN_MARKER_ICON_ID, markerBitmap);
            }
        }
        
        Point point = Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude());
        Feature feature = Feature.fromGeometry(point);
        
        GeoJsonSource source = style.getSourceAs(DEPARTURE_MARKER_SOURCE_ID);
        if (source != null) {
            source.setGeoJson(feature);
        } else {
            source = new GeoJsonSource(DEPARTURE_MARKER_SOURCE_ID, feature);
            style.addSource(source);
            
            SymbolLayer symbolLayer = new SymbolLayer(DEPARTURE_MARKER_LAYER_ID, DEPARTURE_MARKER_SOURCE_ID)
                    .withProperties(
                            PropertyFactory.iconImage(GREEN_MARKER_ICON_ID),
                            PropertyFactory.iconSize(0.15f),
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
        // Hide suggestions immediately
        suggestionsCard.setVisibility(View.GONE);
        
        // Hide keyboard
        hideKeyboard();
        
        // Clear focus to prevent suggestions from showing again
        if (activeEditText != null) {
            activeEditText.clearFocus();
        }
        
        if (activeEditText == etDeparture) {
            etDeparture.setText(prediction.getMainText());
            geocodeAddress(prediction.getDescription(), true);
        } else if (activeEditText == etDestination) {
            etDestination.setText(prediction.getMainText());
            geocodeAddress(prediction.getDescription(), false);
        }
    }
    
    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    
    private void geocodeAddress(String address, boolean isDeparture) {
        if (address.trim().isEmpty()) {
            Toast.makeText(this, "Please enter an address", Toast.LENGTH_SHORT).show();
            return;
        }
        
        executorService.execute(() -> {
            try {
                Geocoder geocoder = new Geocoder(Lab9MapActivity.this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocationName(address, 1);
                
                if (addresses != null && !addresses.isEmpty()) {
                    Address location = addresses.get(0);
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    
                    runOnUiThread(() -> {
                        if (isDeparture) {
                            departureLatLng = latLng;
                            addDepartureMarker(latLng);
                        } else {
                            destinationLatLng = latLng;
                            addDestinationMarker(latLng);
                        }
                        
                        // Auto-calculate route if both locations are set
                        if (departureLatLng != null && destinationLatLng != null) {
                            calculateRoute();
                        }
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
    
    private void calculateRoute() {
        // Validation
        if (departureLatLng == null) {
            Toast.makeText(this, "Please set departure location", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (destinationLatLng == null) {
            Toast.makeText(this, "Please set destination location", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if locations are too close (same location)
        float[] results = new float[1];
        Location.distanceBetween(
                departureLatLng.getLatitude(), departureLatLng.getLongitude(),
                destinationLatLng.getLatitude(), destinationLatLng.getLongitude(),
                results
        );
        
        if (results[0] < 10) { // Less than 10 meters
            Toast.makeText(this, "Departure and destination are the same location", Toast.LENGTH_SHORT).show();
            return;
        }
        
        getRoute(departureLatLng, destinationLatLng);
    }
    
    private void addDestinationMarker(LatLng latLng) {
        Style style = mapLibreMap.getStyle();
        if (style == null) {
            return;
        }
        
        // Add the red marker icon
        if (style.getImage(RED_MARKER_ICON_ID) == null) {
            Bitmap markerBitmap = getBitmapFromVectorDrawable(R.drawable.blue_marker);
            if (markerBitmap != null) {
                style.addImage(RED_MARKER_ICON_ID, markerBitmap);
            }
        }
        
        Point point = Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude());
        Feature feature = Feature.fromGeometry(point);
        
        GeoJsonSource source = style.getSourceAs(DEST_MARKER_SOURCE_ID);
        if (source != null) {
            source.setGeoJson(feature);
        } else {
            source = new GeoJsonSource(DEST_MARKER_SOURCE_ID, feature);
            style.addSource(source);
            
            SymbolLayer symbolLayer = new SymbolLayer(DEST_MARKER_LAYER_ID, DEST_MARKER_SOURCE_ID)
                    .withProperties(
                            PropertyFactory.iconImage(RED_MARKER_ICON_ID),
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
                
                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.getString("code").equals("Ok")) {
                    JSONArray routes = jsonResponse.getJSONArray("routes");
                    if (routes.length() > 0) {
                        JSONObject route = routes.getJSONObject(0);
                        JSONObject geometry = route.getJSONObject("geometry");
                        JSONArray coordinates = geometry.getJSONArray("coordinates");
                        
                        List<Point> points = new ArrayList<>();
                        for (int i = 0; i < coordinates.length(); i++) {
                            JSONArray coord = coordinates.getJSONArray(i);
                            points.add(Point.fromLngLat(coord.getDouble(0), coord.getDouble(1)));
                        }
                        
                        double distance = route.getDouble("distance") / 1000; // km
                        double duration = route.getDouble("duration") / 60; // minutes
                        
                        runOnUiThread(() -> {
                            drawRoute(points);
                            fitBoundsToRoute(start, end);
                            displayRouteInfo(distance, duration);
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(Lab9MapActivity.this, "Route not found", Toast.LENGTH_SHORT).show();
                        distanceCard.setVisibility(View.GONE);
                    });
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(Lab9MapActivity.this, "Error getting route: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    distanceCard.setVisibility(View.GONE);
                });
            }
        });
    }
    
    private void displayRouteInfo(double distance, double duration) {
        tvDistance.setText(String.format(Locale.US, "Distance: %.1f km", distance));
        tvDuration.setText(String.format(Locale.US, "Duration: %.0f min", duration));
        distanceCard.setVisibility(View.VISIBLE);
    }
    
    private void drawRoute(List<Point> points) {
        Style style = mapLibreMap.getStyle();
        if (style == null) {
            return;
        }
        
        LineString lineString = LineString.fromLngLats(points);
        
        GeoJsonSource source = style.getSourceAs(ROUTE_SOURCE_ID);
        if (source != null) {
            source.setGeoJson(lineString);
        } else {
            source = new GeoJsonSource(ROUTE_SOURCE_ID, lineString);
            style.addSource(source);
            
            LineLayer lineLayer = new LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID)
                    .withProperties(
                            PropertyFactory.lineColor(Color.parseColor("#4285F4")),
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
        
        // Clear departure marker if not using current location
        if (!isDepartureUsingCurrentLocation) {
            GeoJsonSource depSource = style.getSourceAs(DEPARTURE_MARKER_SOURCE_ID);
            if (depSource != null) {
                depSource.setGeoJson(FeatureCollection.fromFeatures(new ArrayList<>()));
            }
        }
        
        distanceCard.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Set as departure on first permission grant (app start)
                getCurrentLocation(true);
            } else {
                Toast.makeText(this, "Location permission denied. Cannot use current location.", Toast.LENGTH_LONG).show();
            }
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
