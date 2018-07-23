//package com.syla;
//
//import android.app.FragmentManager;
//import android.content.Context;
//import android.content.IntentSender;
//import android.graphics.Color;
//import android.location.Location;
//import android.os.Build;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.annotation.RequiresApi;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.util.Log;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.Window;
//import android.view.WindowManager;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.ScrollView;
//
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.common.api.PendingResult;
//import com.google.android.gms.common.api.ResultCallback;
//import com.google.android.gms.common.api.Status;
//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationRequest;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.location.LocationSettingsRequest;
//import com.google.android.gms.location.LocationSettingsResult;
//import com.google.android.gms.location.LocationSettingsStatusCodes;
//import com.google.android.gms.location.SettingsClient;
//import com.google.android.gms.maps.CameraUpdate;
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.MapFragment;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.model.BitmapDescriptorFactory;
//import com.google.android.gms.maps.model.CameraPosition;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.LatLngBounds;
//import com.google.android.gms.maps.model.Marker;
//import com.google.android.gms.maps.model.MarkerOptions;
//import com.syla.adapter.LogAdapter;
//import com.syla.application.MyApp;
//import com.syla.utils.LocationProvider;
//import com.syla.utils.ObservableScrollView;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Random;
//
//public class DemoMainActivity extends CustomActivity implements OnMapReadyCallback,
//        ObservableScrollView.OnScrollChangedListener, LocationProvider.LocationCallback, LocationProvider.PermissionCallback {
//
//    private static final String TAG = "mapScreen";
//    private GoogleMap mMap;
//    // location last updated time
//    private FrameLayout imgContainer;
//    private Marker myMarker;
//    private LocationProvider locationProvider;
//
//    // location updates interval - 10sec
//    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
//
//    // fastest updates interval - 5 sec
//    // location updates will be received if another app is requesting the locations
//    // than your app can handle
//    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
//
//    private static final int REQUEST_CHECK_SETTINGS = 100;
//    // bunch of location related apis
//    private FusedLocationProviderClient mFusedLocationClient;
//    private SettingsClient mSettingsClient;
//    private LocationRequest mLocationRequest;
//    private LocationSettingsRequest mLocationSettingsRequest;
//    private Location mCurrentLocation;
//    // boolean flag to toggle the ui
//    private RecyclerView rv_list;
//    private ObservableScrollView mScrollView;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
//            MyApp.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
//        }
//        if (Build.VERSION.SDK_INT >= 19) {
//            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//        }
//        if (Build.VERSION.SDK_INT >= 21) {
//            MyApp.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
//            getWindow().setStatusBarColor(Color.TRANSPARENT);
//        }
//
//        locationProvider = new LocationProvider(getContext(), this, this);
//        DemoMainActivity.this.locationProvider.connect();
//        // restore the values from saved instance state
////        restoreValuesFromBundle(savedInstanceState);
//        mScrollView = findViewById(R.id.scroll_view);
//        mScrollView.setOnScrollChangedListener(this);
//        setupViews();
//    }
//
//    @Override
//    public void onClick(View v) {
//        super.onClick(v);
//        if (v.getId() == R.id.txt_leave_group) {
//            finish();
//        }
//    }
//
//    private MapFragment getMapFragment() {
//        FragmentManager fm = getFragmentManager();
//        return (MapFragment) fm.findFragmentById(R.id.map);
//    }
//
//    private void setupViews() {
//        imgContainer = findViewById(R.id.img_container);
//        imgContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                (int) (MyApp.getDisplayHeight() * .6)));
//        ImageView transparentImageView = findViewById(R.id.transparent_image);
//        setTouchNClick(R.id.txt_leave_group);
//        rv_list = findViewById(R.id.rv_list);
//        rv_list.setLayoutManager(new LinearLayoutManager(getContext()));
//        rv_list.setAdapter(new LogAdapter(getContext(), new ArrayList<String>()));
//
//        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        getMapFragment().getMapAsync(this);
//
//
//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//        mSettingsClient = LocationServices.getSettingsClient(this);
//
//
//        mLocationRequest = new LocationRequest();
//        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
//        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//
//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
//        builder.addLocationRequest(mLocationRequest);
//        mLocationSettingsRequest = builder.build();
//
//
//        transparentImageView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                int action = event.getAction();
//                switch (action) {
//                    case MotionEvent.ACTION_DOWN:
//                        // Disallow ScrollView to intercept touch events.
//                        mScrollView.requestDisallowInterceptTouchEvent(true);
//                        // Disable touch on transparent view
//                        return false;
//                    case MotionEvent.ACTION_UP:
//                        // Allow ScrollView to intercept touch events.
//                        mScrollView.requestDisallowInterceptTouchEvent(false);
//                        return true;
//
//                    case MotionEvent.ACTION_MOVE:
//                        mScrollView.requestDisallowInterceptTouchEvent(true);
//                        return false;
//
//                    default:
//                        return true;
//                }
//            }
//        });
//
//        mScrollView.post(new Runnable() {
//            @Override
//            public void run() {
//                mScrollView.fullScroll(ScrollView.FOCUS_UP);
//            }
//        });
//
//    }
//
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//
//        // Add a marker in Sydney and move the camera
//    }
//
//    /**
//     * Restoring values from saved instance state
//     */
////    private void restoreValuesFromBundle(Bundle savedInstanceState) {
////        if (savedInstanceState != null) {
////            if (savedInstanceState.containsKey("is_requesting_updates")) {
//////                mRequestingLocationUpdates = savedInstanceState.getBoolean("is_requesting_updates");
////            }
////
////            if (savedInstanceState.containsKey("last_known_location")) {
////                mCurrentLocation = savedInstanceState.getParcelable("last_known_location");
////            }
////
////            if (savedInstanceState.containsKey("last_updated_on")) {
////                mLastUpdateTime = savedInstanceState.getString("last_updated_on");
////            }
////        }
////
////    }
//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
////        outState.putBoolean("is_requesting_updates", mRequestingLocationUpdates);
////        outState.putParcelable("last_known_location", mCurrentLocation);
////        outState.putString("last_updated_on", mLastUpdateTime);
//
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (!MyApp.isLocationEnabled(getContext())) {
//            displayLocationSettingsRequest(getContext());
//        }
//    }
//
//
//    private void displayLocationSettingsRequest(Context context) {
//        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
//                .addApi(LocationServices.API).build();
//        googleApiClient.connect();
//
//        LocationRequest locationRequest = LocationRequest.create();
//        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        locationRequest.setInterval(10000);
//        locationRequest.setFastestInterval(10000 / 2);
//
//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
//        builder.setAlwaysShow(true);
//
//        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
//        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
//            @Override
//            public void onResult(LocationSettingsResult result) {
//                final Status status = result.getStatus();
//                switch (status.getStatusCode()) {
//                    case LocationSettingsStatusCodes.SUCCESS:
//                        Log.i(TAG, "All location settings are satisfied.");
//                        break;
//                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
//                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");
//
//                        try {
//                            // Show the dialog by calling startResolutionForResult(), and check the result
//                            // in onActivityResult().
//                            status.startResolutionForResult(DemoMainActivity.this, REQUEST_CHECK_SETTINGS);
//                        } catch (IntentSender.SendIntentException e) {
//                            Log.i(TAG, "PendingIntent unable to execute request.");
//                        }
//                        break;
//                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
//                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
//                        break;
//                }
//            }
//        });
//    }
//
//    private boolean areMarkersSet = false;
//
//    public LatLng getRandomLocation(LatLng point, int radius) {
//        if (areMarkersSet) {
//            return null;
//        }
//
//        List<LatLng> randomPoints = new ArrayList<>();
//        List<Float> randomDistances = new ArrayList<>();
//        Location myLocation = new Location("");
//        myLocation.setLatitude(point.latitude);
//        myLocation.setLongitude(point.longitude);
//        LatLngBounds.Builder builder = new LatLngBounds.Builder();
//        builder.include(new LatLng(this.sourceLocation.getLatitude(), this.sourceLocation.getLongitude()));
//        //This is to generate 10 random points
//        for (int i = 0; i < 5; i++) {
//            double x0 = point.latitude;
//            double y0 = point.longitude;
//
//            Random random = new Random();
//
//            // Convert radius from meters to degrees
//            double radiusInDegrees = radius / 111000f;
//
//            double u = random.nextDouble();
//            double v = random.nextDouble();
//            double w = radiusInDegrees * Math.sqrt(u);
//            double t = 2 * Math.PI * v;
//            double x = w * Math.cos(t);
//            double y = w * Math.sin(t);
//
//            // Adjust the x-coordinate for the shrinking of the east-west distances
//            double new_x = x / Math.cos(y0);
//
//            double foundLatitude = new_x + x0;
//            double foundLongitude = y + y0;
//            LatLng randomLatLng = new LatLng(foundLatitude, foundLongitude);
//            randomPoints.add(randomLatLng);
//            Location l1 = new Location("");
//            l1.setLatitude(randomLatLng.latitude);
//            l1.setLongitude(randomLatLng.longitude);
//            randomDistances.add(l1.distanceTo(myLocation));
//
//            builder.include(this.mMap.addMarker(new MarkerOptions().position(new LatLng(
//                    randomLatLng.latitude,
//                    randomLatLng.longitude))
//                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)))
//                    .getPosition());
//        }
//        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(adjustBoundsForMaxZoomLevel(builder.build()), 50);
//        mScrollView.fullScroll(ScrollView.FOCUS_UP);
//        //Get nearest point to the centre
//        int indexOfNearestPointToCentre = randomDistances.indexOf(Collections.min(randomDistances));
//        return randomPoints.get(indexOfNearestPointToCentre);
//    }
//
//    private Context getContext() {
//        return DemoMainActivity.this;
//    }
//
//
//    @Override
//    public void onScrollChanged(int deltaX, int deltaY) {
//        int scrollY = mScrollView.getScrollY();
//        // Add parallax effect
//        imgContainer.setTranslationY(scrollY * 0.5f);
//    }
//
//    private Location sourceLocation;
//
//    @Override
//    public void handleNewLocation(Location location) {
//        sourceLocation = location;
//        mMap.clear();
////        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),
////                location.getLongitude()), 13f));
//        mMap.addMarker(new MarkerOptions().position(new LatLng(
//                location.getLatitude(),
//                location.getLongitude()))
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_user_marker)));
//        if (this.mMap != null) {
//            this.mMap.getUiSettings().setZoomControlsEnabled(false);
//            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(15.5f).tilt(0.0f).build();
//            if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == 0 || ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") == 0) {
//                this.mMap.setMyLocationEnabled(true);
//            } else {
//                ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"}, 1010);
//            }
//            this.mMap.getUiSettings().setMyLocationButtonEnabled(true);
//            this.mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//        }
//        getRandomLocation(new LatLng(location.getLatitude(), location.getLongitude()), 1000);
//        areMarkersSet = true;
//    }
//
//    @Override
//    public void handleManualPermission() {
//        ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"}, 1010);
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        locationProvider = new LocationProvider(getContext(), this, this);
//    }
//
//    private LatLngBounds adjustBoundsForMaxZoomLevel(LatLngBounds bounds) {
//        LatLng sw = bounds.southwest;
//        LatLng ne = bounds.northeast;
//        double deltaLat = Math.abs((sw.latitude - this.sourceLocation.getLatitude())
//                - (ne.latitude - this.sourceLocation.getLatitude()));
//        double deltaLon = Math.abs((sw.longitude - this.sourceLocation.getLongitude())
//                - (ne.longitude - this.sourceLocation.getLongitude()));
//        LatLng latLng;
//        LatLng ne2;
//        LatLngBounds latLngBounds;
//        if (deltaLat < 0.005d) {
//            latLng = new LatLng(sw.latitude - (0.005d - (deltaLat / 2.0d)), sw.longitude);
//            ne2 = new LatLng(ne.latitude + (0.005d - (deltaLat / 2.0d)), ne.longitude);
//            latLngBounds = new LatLngBounds(latLng, ne2);
//            ne = ne2;
//            sw = latLng;
//        } else if (deltaLon < 0.005d) {
//            latLng = new LatLng(sw.latitude, sw.longitude - (0.005d - (deltaLon / 2.0d)));
//            ne2 = new LatLng(ne.latitude, ne.longitude + (0.005d - (deltaLon / 2.0d)));
//            latLngBounds = new LatLngBounds(latLng, ne2);
//            ne = ne2;
//            sw = latLng;
//        }
//        LatLngBounds.Builder displayBuilder = new LatLngBounds.Builder();
//        displayBuilder.include(new LatLng(this.sourceLocation.getLatitude(), this.sourceLocation.getLongitude()));
//        displayBuilder.include(new LatLng(this.sourceLocation.getLatitude()
//                + deltaLat, this.sourceLocation.getLongitude() + deltaLon));
//        displayBuilder.include(new LatLng(this.sourceLocation.getLatitude()
//                - deltaLat, this.sourceLocation.getLongitude() - deltaLon));
//        this.mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(displayBuilder.build(), 100));
//        this.mMap.setMaxZoomPreference(15.5f);
//        return bounds;
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private static void setTranslucentStatusBarLollipop(Window window) {
//        window.setStatusBarColor(
//                window.getContext()
//                        .getResources()
//                        .getColor(R.color.transparent));
//    }
//}
