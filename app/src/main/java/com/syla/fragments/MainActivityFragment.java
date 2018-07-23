//package com.syla.fragments;
//
//import android.Manifest;
//import android.app.Fragment;
//import android.app.FragmentManager;
//import android.content.pm.PackageManager;
//import android.os.Build;
//import android.os.Bundle;
//import android.support.v4.app.ActivityCompat;
//import android.view.LayoutInflater;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.MapFragment;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.UiSettings;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.MarkerOptions;
//import com.syla.R;
//import com.syla.utils.ObservableScrollView;
//
//public class MainActivityFragment extends Fragment implements
//        ObservableScrollView.OnScrollChangedListener, OnMapReadyCallback {
//    private ObservableScrollView mScrollView;
//    View rootView;
//    private View imgContainer;
//    private GoogleMap mMap;
//
//    public MainActivityFragment() {
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        rootView = inflater.inflate(R.layout.fragment_main, container, false);
//        // Init your layout and set your listener
//        mScrollView = (ObservableScrollView) rootView.findViewById(R.id.scroll_view);
//        mScrollView.setOnScrollChangedListener(this);
//        // Store the reference of your image container
//        imgContainer = rootView.findViewById(R.id.img_container);
//        ImageView transparentImageView = (ImageView)
//                rootView.findViewById(R.id.transparent_image);
//        getMapFragment().getMapAsync(this);
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
//        return rootView;
//    }
//
//    @Override
//    public void onScrollChanged(int deltaX, int deltaY) {
//        int scrollY = mScrollView.getScrollY();
//        // Add parallax effect
//        imgContainer.setTranslationY(scrollY * 0.1f);
//    }
//
//    private MapFragment getMapFragment() {
//        FragmentManager fm = null;
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            fm = getFragmentManager();
//        } else {
//            fm = getChildFragmentManager();
//        }
//        return (MapFragment) fm.findFragmentById(R.id.map);
//    }
//
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//        final UiSettings settings = mMap.getUiSettings();
//        settings.setCompassEnabled(true);
//        settings.setMyLocationButtonEnabled(true);
//        settings.setZoomControlsEnabled(true);
//        if (ActivityCompat.checkSelfPermission(getActivity(),
//                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
//                        PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        mMap.setMyLocationEnabled(true);
//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//    }
//}