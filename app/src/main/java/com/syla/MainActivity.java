package com.syla;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.syla.adapter.LogAdapter;
import com.syla.application.AppConstants;
import com.syla.application.MyApp;
import com.syla.models.Users;
import com.syla.utils.LocationProvider;
import com.syla.utils.ObservableScrollView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends CustomActivity implements OnMapReadyCallback,
        ObservableScrollView.OnScrollChangedListener, LocationProvider.LocationCallback, LocationProvider.PermissionCallback {

    private static final String TAG = "mapScreen";
    private GoogleMap mMap;
    private FrameLayout imgContainer;
    private LocationProvider locationProvider;
    private static final int REQUEST_CHECK_SETTINGS = 100;
    private RecyclerView rv_list;
    private ObservableScrollView mScrollView;
    private LinearLayout ll_bottom;
    private Toolbar toolbar;
    private RelativeLayout rl_location;
    private String currentRoomId;
    private boolean isNewRoom = false;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<Users> users = new ArrayList<>();
    private boolean isMine = false;
    private Button txt_delete_group;
    private Switch switch_location_on_off;
    private UpdateCallbacks updateCallbacks;
    private Users admin;
    private TextView txt_group_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentRoomId = MyApp.getSharedPrefString(AppConstants.CURRENT_ROOM_ID);
        isNewRoom = getIntent().getBooleanExtra("isNew", false);
        isMine = getIntent().getBooleanExtra("isMine", false);
        setContentView(R.layout.activity_main);
        txt_group_info = findViewById(R.id.txt_group_info);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        txt_group_info.setVisibility(View.GONE);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Room Name");
        }
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            MyApp.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            MyApp.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        locationProvider = new LocationProvider(getContext(), this, this);
        MainActivity.this.locationProvider.connect();
        // restore the values from saved instance state
//        restoreValuesFromBundle(savedInstanceState);
        mScrollView = findViewById(R.id.scroll_view);
        mScrollView.setOnScrollChangedListener(this);
        setupViews();

        if (isNewRoom) {
            AlertDialog.Builder b = new AlertDialog.Builder(getContext());
            b.setTitle("Invite People").setMessage("You have created a new room, you can share the room id to" +
                    " other people to join you.")
                    .setPositiveButton("Share Now", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String link_val = currentRoomId;
                            String body = "Hi, I have created a room to share our location, so that we can track each other anytime" +
                                    "\n'" + link_val
                                    + "' is the room id you have to enter to join it.";
//                            String shareBody = "Hi, I have created a room to share our location, so that we can track each other anytime" +
//                                    "\n'" + currentRoomId + "' is the room id you have to enter to join it.";
                            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                            sharingIntent.setType("text/plain");
                            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Join Room");
                            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
                            startActivity(Intent.createChooser(sharingIntent, "Share Via"));
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).create().show();
        }

        // get all data for the room
        db.collection("allRooms").document(currentRoomId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(final DocumentSnapshot snapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("Firestore Demo", "Listen failed.", e);
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    Log.d("Firestore Demo", "Current data: " + snapshot.getData());
                    String roomName1 = snapshot.getString("roomName");
                    toolbar.setTitle(roomName1);
                    if (isMine) {

                    } else {
                        if (snapshot.getBoolean("isLeft")) {
                            return;
                        }
                        admin = new Users();
                        admin.setActive(true);
                        try {
                            admin.setLat(snapshot.getDouble("lat"));
                            admin.setLng(snapshot.getDouble("lng"));
                        } catch (Exception ee) {
                        }
                        admin.setUserId(snapshot.getString("userId"));
                        admin.setAdmin(true);
                        admin.setName(snapshot.getString("userName"));
                    }

                    snapshot.getReference().collection("Users").addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(QuerySnapshot userSnapshots, FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w("Firestore Demo", "Listen failed for users.", e);
                                txt_group_info.setVisibility(View.VISIBLE);
                                return;
                            }
                            users.clear();
                            if (!isMine)
                                users.add(admin);
                            if (userSnapshots != null && !userSnapshots.isEmpty()) {
                                for (DocumentSnapshot documentSnapshot : userSnapshots) {
                                    if (documentSnapshot.exists()) {
                                        Log.d(TAG, "onSuccess: DOCUMENT" + documentSnapshot.getId()
                                                + " ; " + documentSnapshot.getData());
                                        Users u = documentSnapshot.toObject(Users.class);
                                        if (!u.getUserId().equals(MyApp.getSharedPrefString(AppConstants.USER_ID))) {
                                            if (!u.isRemoved()) {
                                                users.add(u);
                                            } else {
                                                MyApp.popMessage("Alert", u.getName() + " has been left the room", getContext());
                                                db.collection("allRooms").document(currentRoomId).collection("Users")
                                                        .document(u.getUserId())
                                                        .delete()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Log.d(TAG, "You have been removed ");
                                                            }
                                                        });
                                            }
                                        } else if (u.isDeleted()) {
                                            db.collection("allRooms").document(currentRoomId).collection("Users")
                                                    .document(u.getUserId())
                                                    .delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            MyApp.popFinishableMessage("Alert!",
                                                                    "You have been removed by the admin.", MainActivity.this);
                                                        }
                                                    });
                                        }

                                    }
                                }

                                if (users.size() == 0)
                                    txt_group_info.setVisibility(View.VISIBLE);
                                else
                                    txt_group_info.setVisibility(View.GONE);

                                rv_list.setAdapter(new LogAdapter(getContext(), users, isMine));
                                if (sourceLocation != null && isVisible) {
                                    areMarkersSet = false;
                                    setupUsersMarker(sourceLocation, users);
                                }

                            } else {
                                // No users found yet
                                txt_group_info.setVisibility(View.VISIBLE);
                            }
                        }
                    });

                } else {
                    Log.d("Firestore Demo", "Current data: null");
                    MyApp.popFinishableMessage("Alert!"
                            , "This room has been deleted by your admin, you cannot" +
                                    " access it anymore.\nThank you", MainActivity.this);
                }
            }
        });

        if (isMine) {
            txt_delete_group.setVisibility(View.VISIBLE);
        }

        updateCallbacks = new UpdateCallbacks() {
            @Override
            public void updateInvisible(final boolean isVisible) {
                db.collection("allRooms").document(currentRoomId).collection("Users")
                        .document(MyApp.getSharedPrefString(AppConstants.USER_ID))
                        .update("isActive", isVisible)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "location updated with value " + isVisible);
                            }
                        });
            }

            @Override
            public void updateSave(boolean isSaved) {

            }

            @Override
            public void updateRemoved(boolean isRemoved) {

            }


        };
    }


    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.txt_leave_group) {
            if (isMine) {
                AlertDialog.Builder b = new AlertDialog.Builder(getContext());
                b.setTitle("Leaving Group?").setMessage("Are you sure that you want to leave the group. You cannot " +
                        "get any update with the group and you will be removed.")
                        .setPositiveButton("Leave", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                MyApp.showMassage(getContext(), "Removing you.");
                                db.collection("allRooms").document(currentRoomId).update("isLeft", true)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                finish();
                                            }
                                        });
                            }
                        }).setNegativeButton("Stay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();

            } else {
                AlertDialog.Builder b = new AlertDialog.Builder(getContext());
                b.setTitle("Leaving Group?").setMessage("Are you sure that you want to leave the group. You cannot " +
                        "get any update with the group and you will be removed.")
                        .setPositiveButton("Leave", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                MyApp.showMassage(getContext(), "Removing you.");
                                db.collection("allRooms").document(currentRoomId).collection("Users")
                                        .document(MyApp.getSharedPrefString(AppConstants.USER_ID))
                                        .update("isRemoved", true)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "You have been removed ");
                                                finish();
                                            }
                                        });
                            }
                        }).setNegativeButton("Stay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
            }

        } else if (v == txt_delete_group) {
            AlertDialog.Builder b = new AlertDialog.Builder(getContext());
            b.setTitle("Delete Room?").setMessage("Are you sure that you want to delete this room permanently?")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            db.collection("allRooms").document(currentRoomId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    if (isMine) {
                                        finish();
                                    } else {
                                        db.collection("allRooms").document(currentRoomId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                            @Override
                                            public void onEvent(final DocumentSnapshot snapshot, FirebaseFirestoreException e) {
                                                if (e != null) {
                                                    Log.w("Firestore Demo", "Listen failed.", e);
                                                    MyApp.popFinishableMessage("Alert!"
                                                            , "This room has been deleted by your admin, you cannot" +
                                                                    " access it anymore.\nThank you", MainActivity.this);
                                                    return;
                                                }
                                                if (snapshot != null && snapshot.exists()) {
                                                    Log.d("Firestore Demo", "Current data: " + snapshot.getData());
                                                    String roomName1 = snapshot.getString("roomName");
                                                    toolbar.setTitle(roomName1);
                                                    if (isMine) {

                                                    } else {
                                                        if (snapshot.getBoolean("isLeft")) {
                                                            return;
                                                        }
                                                        admin = new Users();
                                                        admin.setActive(true);
                                                        try {
                                                            admin.setLat(snapshot.getDouble("lat"));
                                                            admin.setLng(snapshot.getDouble("lng"));
                                                        } catch (Exception ee) {
                                                        }
                                                        admin.setUserId(snapshot.getString("userId"));
                                                        admin.setAdmin(true);
                                                        admin.setName(snapshot.getString("userName"));
                                                    }

                                                    snapshot.getReference().collection("Users").addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onEvent(QuerySnapshot userSnapshots, FirebaseFirestoreException e) {
                                                            if (e != null) {
                                                                Log.w("Firestore Demo", "Listen failed for users.", e);
                                                                return;
                                                            }
                                                            users.clear();
                                                            if (!isMine)
                                                                users.add(admin);
                                                            if (userSnapshots != null && !userSnapshots.isEmpty()) {
                                                                for (DocumentSnapshot documentSnapshot : userSnapshots) {
                                                                    if (documentSnapshot.exists()) {
                                                                        Log.d(TAG, "onSuccess: DOCUMENT" + documentSnapshot.getId()
                                                                                + " ; " + documentSnapshot.getData());
                                                                        Users u = documentSnapshot.toObject(Users.class);
                                                                        if (!u.getUserId().equals(MyApp.getSharedPrefString(AppConstants.USER_ID))) {
                                                                            if (!u.isRemoved()) {
                                                                                users.add(u);
                                                                            } else {
                                                                                MyApp.popMessage("Alert", u.getName() + " has been left the room", getContext());
                                                                                db.collection("allRooms").document(currentRoomId).collection("Users")
                                                                                        .document(u.getUserId())
                                                                                        .delete()
                                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                            @Override
                                                                                            public void onSuccess(Void aVoid) {
                                                                                                Log.d(TAG, "You have been removed ");
                                                                                            }
                                                                                        });
                                                                            }
                                                                        } else if (u.isDeleted()) {
                                                                            db.collection("allRooms").document(currentRoomId).collection("Users")
                                                                                    .document(u.getUserId())
                                                                                    .delete()
                                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                        @Override
                                                                                        public void onSuccess(Void aVoid) {
                                                                                            MyApp.popFinishableMessage("Alert!",
                                                                                                    "You have been removed by the admin.", MainActivity.this);
                                                                                        }
                                                                                    });
                                                                        }

                                                                    }
                                                                }

                                                                if (users.size() == 0) {
                                                                    MyApp.popFinishableMessage("Alert!"
                                                                            , "This room has been deleted by your admin, you cannot" +
                                                                                    " access it anymore.\nThank you", MainActivity.this);
                                                                    return;
                                                                }
                                                                rv_list.setAdapter(new LogAdapter(getContext(), users, isMine));
                                                                if (sourceLocation != null && isVisible) {
                                                                    areMarkersSet = false;
                                                                    setupUsersMarker(sourceLocation, users);
                                                                }

                                                            } else {
                                                                MyApp.popFinishableMessage("Alert!"
                                                                        , "This room has been deleted by your admin, you cannot" +
                                                                                " access it anymore.\nThank you", MainActivity.this);
                                                            }
                                                        }
                                                    });

                                                } else {
                                                    Log.d("Firestore Demo", "Current data: null");
                                                    MyApp.popFinishableMessage("Alert!"
                                                            , "This room has been deleted by your admin, you cannot" +
                                                                    " access it anymore.\nThank you", MainActivity.this);
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();
        } else if (v.getId() == R.id.btn_share_room) {
            String link_val = currentRoomId;
            String body = "Hi, I have created a room to share our location, so that we can track each other anytime" +
                    "\n'" + link_val
                    + "' is the room id you have to enter to join it.";
//                            String shareBody = "Hi, I have created a room to share our location, so that we can track each other anytime" +
//                                    "\n'" + currentRoomId + "' is the room id you have to enter to join it.";
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Join Room");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
            startActivity(Intent.createChooser(sharingIntent, "Share Via"));
        }
    }

    private MapFragment getMapFragment() {
        FragmentManager fm = getFragmentManager();
        return (MapFragment) fm.findFragmentById(R.id.map);
    }

    private void setupViews() {
        switch_location_on_off = findViewById(R.id.switch_location_on_off);
        txt_delete_group = findViewById(R.id.txt_delete_group);
        rl_location = findViewById(R.id.rl_location);
        imgContainer = findViewById(R.id.img_container);
        ll_bottom = findViewById(R.id.ll_bottom);
        imgContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (MyApp.getDisplayHeight() * 0.98)));
        ll_bottom.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                MyApp.getDisplayHeight()));
        ImageView transparentImageView = findViewById(R.id.transparent_image);
        setTouchNClick(R.id.txt_leave_group);
        setTouchNClick(R.id.btn_share_room);
        rv_list = findViewById(R.id.rv_list);
        rv_list.setLayoutManager(new LinearLayoutManager(getContext()));

        setTouchNClick(R.id.txt_delete_group);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        getMapFragment().getMapAsync(this);


        transparentImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        mScrollView.requestDisallowInterceptTouchEvent(true);
                        // Disable touch on transparent view
                        return false;
                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        mScrollView.requestDisallowInterceptTouchEvent(false);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        mScrollView.requestDisallowInterceptTouchEvent(true);
                        return false;

                    default:
                        return true;
                }
            }
        });

        mScrollView.post(new Runnable() {
            @Override
            public void run() {
                mScrollView.smoothScrollTo(0, MyApp.getDisplayHeight() / 3);
            }
        });

        switch_location_on_off.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isVisible = true;
                } else {
                    isVisible = false;
                }
                updateCallbacks.updateInvisible(isVisible);
            }
        });
    }

    private boolean isVisible = true;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
    }

    /**
     * Restoring values from saved instance state
     */
//    private void restoreValuesFromBundle(Bundle savedInstanceState) {
//        if (savedInstanceState != null) {
//            if (savedInstanceState.containsKey("is_requesting_updates")) {
////                mRequestingLocationUpdates = savedInstanceState.getBoolean("is_requesting_updates");
//            }
//
//            if (savedInstanceState.containsKey("last_known_location")) {
//                mCurrentLocation = savedInstanceState.getParcelable("last_known_location");
//            }
//
//            if (savedInstanceState.containsKey("last_updated_on")) {
//                mLastUpdateTime = savedInstanceState.getString("last_updated_on");
//            }
//        }
//
//    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        outState.putBoolean("is_requesting_updates", mRequestingLocationUpdates);
//        outState.putParcelable("last_known_location", mCurrentLocation);
//        outState.putString("last_updated_on", mLastUpdateTime);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!MyApp.isLocationEnabled(getContext())) {
            displayLocationSettingsRequest(getContext());
        }
    }


    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

    private boolean areMarkersSet = false;
    private Runnable updateTask = new Runnable() {
        @Override
        public void run() {
            areMarkersSet = false;
            h.postDelayed(updateTask, 5000);
        }
    };

    private Handler h = new Handler();

    public void setupUsersMarker(Location myLocation, List<Users> users) {
        if (areMarkersSet) {
            return;
        }

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(new LatLng(
                myLocation.getLatitude(),
                myLocation.getLongitude()))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_user_marker)));
        if (this.mMap != null) {
            this.mMap.getUiSettings().setZoomControlsEnabled(false);
            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(myLocation.getLatitude(),
                    myLocation.getLongitude())).zoom(15.5f).tilt(0.0f).build();
            if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == 0 || ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") == 0) {
                this.mMap.setMyLocationEnabled(true);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"}, 1010);
            }
            this.mMap.getUiSettings().setMyLocationButtonEnabled(true);
            this.mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(new LatLng(this.sourceLocation.getLatitude(), this.sourceLocation.getLongitude()));
        //This is to generate 10 random points
        for (int i = 0; i < users.size(); i++) {
            double x0 = myLocation.getLatitude();
            double y0 = myLocation.getLongitude();
            Random random = new Random();

            // Convert radius from meters to degrees
            double radiusInDegrees = 50 / 111000f;

            double u = random.nextDouble();
            double v = random.nextDouble();
            double w = radiusInDegrees * Math.sqrt(u);
            double t = 2 * Math.PI * v;
            double x = w * Math.cos(t);
            double y = w * Math.sin(t);
            // Adjust the x-coordinate for the shrinking of the east-west distances
            double new_x = x / Math.cos(y0);

            double foundLatitude = new_x + users.get(i).getLat();
            double foundLongitude = y + users.get(i).getLng();

            Log.d(TAG, users.get(i).getLat() + " & " + users.get(i).getLng());
            builder.include(this.mMap.addMarker(new MarkerOptions().position(new LatLng(
                    foundLatitude,
                    foundLongitude)).title(users.get(i).getName())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)))
                    .getPosition());
        }
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(adjustBoundsForMaxZoomLevel(builder.build()), 50);
        //Get nearest point to the centre
        h.postDelayed(updateTask, 5000);
    }

    private Context getContext() {
        return MainActivity.this;
    }

    @Override
    public void onScrollChanged(int deltaX, int deltaY) {
        int scrollY = mScrollView.getScrollY();
        // Add parallax effect
        imgContainer.setTranslationY(scrollY * 0.5f);
        Log.d("scrolling", mScrollView.getScrollY() + "");
        int toolbarOffset = MyApp.getDisplayHeight() - mScrollView.getScrollY();
        if (toolbarOffset <= 60) {
            rl_location.setVisibility(View.GONE);
            toolbar.setVisibility(View.VISIBLE);
//            slideUp(toolbar);
//            slideDown(rl_location);

        } else {
            toolbar.setVisibility(View.GONE);
            rl_location.setVisibility(View.VISIBLE);
//            slideUp(rl_location);
//            slideDown(toolbar);
        }
    }

    private Location sourceLocation = null;

    @Override
    public void handleNewLocation(Location location) {
        sourceLocation = location;
        setupUsersMarker(location, users);
        areMarkersSet = true;
    }

    @Override
    public void handleManualPermission() {
        ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"}, 1010);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationProvider = new LocationProvider(getContext(), this, this);
    }

    private LatLngBounds adjustBoundsForMaxZoomLevel(LatLngBounds bounds) {
        LatLng sw = bounds.southwest;
        LatLng ne = bounds.northeast;
        double deltaLat = Math.abs((sw.latitude - this.sourceLocation.getLatitude())
                - (ne.latitude - this.sourceLocation.getLatitude()));
        double deltaLon = Math.abs((sw.longitude - this.sourceLocation.getLongitude())
                - (ne.longitude - this.sourceLocation.getLongitude()));
        LatLng latLng;
        LatLng ne2;
        LatLngBounds latLngBounds;
        if (deltaLat < 0.005d) {
            latLng = new LatLng(sw.latitude - (0.005d - (deltaLat / 2.0d)), sw.longitude);
            ne2 = new LatLng(ne.latitude + (0.005d - (deltaLat / 2.0d)), ne.longitude);
            latLngBounds = new LatLngBounds(latLng, ne2);
            ne = ne2;
            sw = latLng;
        } else if (deltaLon < 0.005d) {
            latLng = new LatLng(sw.latitude, sw.longitude - (0.005d - (deltaLon / 2.0d)));
            ne2 = new LatLng(ne.latitude, ne.longitude + (0.005d - (deltaLon / 2.0d)));
            latLngBounds = new LatLngBounds(latLng, ne2);
            ne = ne2;
            sw = latLng;
        }
        LatLngBounds.Builder displayBuilder = new LatLngBounds.Builder();
        displayBuilder.include(new LatLng(this.sourceLocation.getLatitude(), this.sourceLocation.getLongitude()));
        displayBuilder.include(new LatLng(this.sourceLocation.getLatitude()
                + deltaLat, this.sourceLocation.getLongitude() + deltaLon));
        displayBuilder.include(new LatLng(this.sourceLocation.getLatitude()
                - deltaLat, this.sourceLocation.getLongitude() - deltaLon));
        this.mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(displayBuilder.build(), 100));
        this.mMap.setMaxZoomPreference(15.5f);
        return bounds;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static void setTranslucentStatusBarLollipop(Window window) {
        window.setStatusBarColor(
                window.getContext()
                        .getResources()
                        .getColor(R.color.transparent));
    }

    public void slideUp(View view) {
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                view.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    public void slideDown(View view) {
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,                 // fromYDelta
                view.getHeight()); // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    public void goToUser(Users users) {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("geo:0,0?q=" + users.getLat() + "," + users.getLng() + " (" + users.getName() + ")"));
        startActivity(intent);
    }

    public void deleteUser(final Users users) {
        AlertDialog.Builder b = new AlertDialog.Builder(getContext());
        b.setTitle("Alert!").setMessage("Are you sure to remove '" + users.getName() + "' ?\n")
                .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        db.collection("allRooms").document(currentRoomId).collection("Users")
                                .document(users.getUserId())
                                .update("isDeleted", true)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "You have been removed ");
                                    }
                                });
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }

    private interface UpdateCallbacks {
        void updateInvisible(boolean isVisible);

        void updateSave(boolean isSaved);

        void updateRemoved(boolean isRemoved);
    }
}
