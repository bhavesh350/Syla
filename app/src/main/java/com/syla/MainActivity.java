package com.syla;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
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
import android.widget.ImageButton;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private TextView txt_copy_code;
    private ImageButton btn_share_room;

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
            actionBar.setDisplayHomeAsUpEnabled(false);
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
        locationProvider.connect();
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
                                                if (users.size() > 1) {
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
                if (isMine) {

                } else {
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
            if (isMine) {
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
            } else {
                AlertDialog.Builder b = new AlertDialog.Builder(getContext());
                b.setTitle("Save Group?").setMessage("Save a group will track a record for Saved Room section, where you can" +
                        " access your saved room anytime.\nThank you.")
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                MyApp.showMassage(getContext(), "Saving...");
                                db.collection("allRooms").document(currentRoomId).collection("Users")
                                        .document(MyApp.getSharedPrefString(AppConstants.USER_ID))
                                        .update("isSaved", true)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "Room has been saved. ");
                                                MyApp.showMassage(getContext(), "This room has been Saved");
                                                db.collection("users")
                                                        .document(MyApp.getSharedPrefString(AppConstants.USER_ID))
                                                        .collection("savedRooms")
                                                        .document(currentRoomId)
                                                        .set(new HashMap<String, Object>())
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                            }
                                                        });

//                                                finish();
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

        } else if (v == txt_copy_code) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Copy Code", currentRoomId);
            clipboard.setPrimaryClip(clip);
            MyApp.showMassage(getContext(), "Copied...");
        }
    }

    private MapFragment getMapFragment() {
        FragmentManager fm = getFragmentManager();
        return (MapFragment) fm.findFragmentById(R.id.map);
    }

    private void setupViews() {
        txt_copy_code = findViewById(R.id.txt_copy_code);
        setTouchNClick(R.id.txt_copy_code);
        txt_copy_code.setText(currentRoomId);
        if (isMine) {
            txt_copy_code.setVisibility(View.VISIBLE);
        }

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
        btn_share_room = findViewById(R.id.btn_share_room);
        if (isMine)
            btn_share_room.setImageResource(R.drawable.ic_menu_share);
        else {
            if (MyApp.getStatus(AppConstants.IS_GUEST)) {
                btn_share_room.setVisibility(View.GONE);
            }
            btn_share_room.setImageResource(R.drawable.ic_bookmark);
        }


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
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                Log.d("Current map location ", location.getLatitude() + " , " + location.getLongitude());
                if (myMarker != null) {
                    myMarker.remove();
                }
                if (areMarkersSet) {
                    return;
                }
                sourceLocation = location;
                setupUsersMarker(location, users);
                areMarkersSet = true;
                if (isMine) {
                    db.collection("allRooms").document(currentRoomId)
                            .update("lat", location.getLatitude())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                }
                            });
                    db.collection("allRooms").document(currentRoomId)
                            .update("lng", location.getLongitude())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                }
                            });
                } else {
                    db.collection("allRooms").document(currentRoomId).collection("Users")
                            .document(MyApp.getSharedPrefString(AppConstants.USER_ID))
                            .update("lat", location.getLatitude())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                }
                            });
                    db.collection("allRooms").document(currentRoomId).collection("Users")
                            .document(MyApp.getSharedPrefString(AppConstants.USER_ID))
                            .update("lng", location.getLongitude())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                }
                            });
                }
            }
        });

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
    private Marker myMarker = null;
    private List<Marker> markers = new ArrayList<>();
    private Map<String, Marker> markerMap = new HashMap<>();

    public void setupUsersMarker(Location myLocation, List<Users> users) {
        if (areMarkersSet) {
            return;
        }
        if (lastUserPath != null) {
            String url = getMapsApiDirectionsUrl(new LatLng(sourceLocation.getLatitude(), sourceLocation.getLongitude()),
                    new LatLng(lastUserPath.getLat(), lastUserPath.getLng()));
            new ReadTask().execute(new String[]{url});
        }
//        mMap.clear();
//        myMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(
//                myLocation.getLatitude(),
//                myLocation.getLongitude()))
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_user_marker)));
        if (this.mMap != null) {
            this.mMap.getUiSettings().setZoomControlsEnabled(true);
            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(myLocation.getLatitude(),
                    myLocation.getLongitude())).zoom(15.5f).tilt(0.0f).build();
            if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == 0 || ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") == 0) {
                this.mMap.setMyLocationEnabled(true);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"}, 1010);
            }
            this.mMap.getUiSettings().setMyLocationButtonEnabled(false);
            this.mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(new LatLng(this.sourceLocation.getLatitude(), this.sourceLocation.getLongitude()));
        //This is to generate 10 random points
        for (int i = 0; i < users.size(); i++) {
//            double x0 = myLocation.getLatitude();
//            double y0 = myLocation.getLongitude();
//            Random random = new Random();
//
//            // Convert radius from meters to degrees
//            double radiusInDegrees = 50 / 111000f;
//
//            double u = random.nextDouble();
//            double v = random.nextDouble();
//            double w = radiusInDegrees * Math.sqrt(u);
//            double t = 2 * Math.PI * v;
//            double x = w * Math.cos(t);
//            double y = w * Math.sin(t);
//            // Adjust the x-coordinate for the shrinking of the east-west distances
//            double new_x = x / Math.cos(y0);
//
//            double foundLatitude = new_x + users.get(i).getLat();
//            double foundLongitude = y + users.get(i).getLng();

            Log.d(TAG, users.get(i).getLat() + " & " + users.get(i).getLng());
            if (!markerMap.containsKey(users.get(i).getUserId())) {
                markerMap.put(users.get(i).getUserId(), mMap.addMarker(new MarkerOptions().position(new LatLng(
                        users.get(i).getLat(),
                        users.get(i).getLng())).title(users.get(i).getName())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker))));
            } else {
                Marker m = markerMap.get(users.get(i).getUserId());
                m.setPosition(new LatLng(users.get(i).getLat(), users.get(i).getLng()));
            }


//            markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(
//                    users.get(i).getLat(),
//                    users.get(i).getLng())).title(users.get(i).getName())
//                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker))));
            builder.include(new LatLng(users.get(i).getLat(), users.get(i).getLng()));
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
        if (myMarker != null) {
            myMarker.remove();
        }
        Log.d("Current location ", location.getLatitude() + " , " + location.getLongitude());
        sourceLocation = location;
        setupUsersMarker(location, users);
        areMarkersSet = true;
        if (isMine) {
            db.collection("allRooms").document(currentRoomId)
                    .update("lat", sourceLocation.getLatitude())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                        }
                    });
            db.collection("allRooms").document(currentRoomId)
                    .update("lng", sourceLocation.getLongitude())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                        }
                    });
        } else {
            db.collection("allRooms").document(currentRoomId).collection("Users")
                    .document(MyApp.getSharedPrefString(AppConstants.USER_ID))
                    .update("lat", sourceLocation.getLatitude())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                        }
                    });
            db.collection("allRooms").document(currentRoomId).collection("Users")
                    .document(MyApp.getSharedPrefString(AppConstants.USER_ID))
                    .update("lng", sourceLocation.getLongitude())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                        }
                    });
        }


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

    private Users lastUserPath = null;

    public void goToUser(Users users) {
        MyApp.showMassage(getContext(), "Drawing route");
        lastUserPath = users;
        String url = getMapsApiDirectionsUrl(new LatLng(sourceLocation.getLatitude(), sourceLocation.getLongitude()),
                new LatLng(users.getLat(), users.getLng()));
        new ReadTask().execute(new String[]{url});

//        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
//                Uri.parse("geo:0,0?q=" + users.getLat() + "," + users.getLng() + " (" + users.getName() + ")"));
//        startActivity(intent);
    }


    public void goToGoogleMap(Users users) {

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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (MyApp.getStatus(AppConstants.IS_GUEST)) {
            db.collection("allRooms").document(currentRoomId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                }
            });
        } else {

        }
    }

    private String getMapsApiDirectionsUrl(LatLng origin, LatLng dest) {

        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        return "https://maps.googleapis.com/maps/api/directions/" + "json" + "?" +
                (str_origin + "&" + ("destination=" + dest.latitude + "," + dest.longitude) + "&" + "sensor=false");
    }

    private class ReadTask extends AsyncTask<String, Void, String> {
        private ReadTask() {
        }

        protected String doInBackground(String... url) {
            String data = "";
            try {
                data = new MapHttpConnection().readUr(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(new String[]{result});
        }
    }

    public class MapHttpConnection {
        public String readUr(String mapsApiDirectionsUrl) throws IOException {
            String data = "";
            InputStream istream = null;
            HttpURLConnection urlConnection = null;
            try {
                urlConnection = (HttpURLConnection) new URL(mapsApiDirectionsUrl).openConnection();
                urlConnection.connect();
                istream = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(istream));
                StringBuffer sb = new StringBuffer();
                String str = "";
                while (true) {
                    str = br.readLine();
                    if (str == null) {
                        break;
                    }
                    sb.append(str);
                }
                data = sb.toString();
                br.close();
            } catch (Exception e) {
                Log.d("Exception url", e.toString());
            } finally {
                istream.close();
                urlConnection.disconnect();
            }
            return data;
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        private ParserTask() {
        }

        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            List<List<HashMap<String, String>>> routes = null;
            try {
                routes = new PathJSONParser().parse(new JSONObject(jsonData[0]));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            if (polyline != null) {
                polyline.remove();
            }


            PolylineOptions polyLineOptions = null;
            for (int i = 0; i < routes.size(); i++) {
                ArrayList<LatLng> points = new ArrayList();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = (List) routes.get(i);
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = (HashMap) path.get(j);
                    if (j == 0) {
                    } else if (j == 1) {
                    } else {
                        points.add(new LatLng(Double.parseDouble(point.get("lat")), Double.parseDouble((String) point.get("lng"))));
                    }
                }
                polyLineOptions.addAll(points);
                polyLineOptions.width(10.0f);
                polyLineOptions.color(Color.parseColor("#156CB3"));
            }

            if (removedLine != null) {
                removedLine.remove();
            }
            try {

                polyline = mMap.addPolyline(polyLineOptions);
                removedPolyLine = polyLineOptions;
            } catch (Exception e) {
                if (removedPolyLine != null) {
                    removedLine = mMap.addPolyline(removedPolyLine);
                }
//                MyApp.showMassage(getContext(), "Path is too short to draw");
            }
        }
    }

    private PolylineOptions removedPolyLine = null;
    private Polyline polyline = null;
    private Polyline removedLine = null;

    public class PathJSONParser {
        public List<List<HashMap<String, String>>> parse(JSONObject jObject) {
            List<List<HashMap<String, String>>> routes = new ArrayList();
            try {
                JSONArray jRoutes = jObject.getJSONArray("routes");
                for (int i = 0; i < jRoutes.length(); i++) {
                    JSONArray jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                    List<HashMap<String, String>> path = new ArrayList();
                    for (int j = 0; j < jLegs.length(); j++) {
                        JSONObject jDistance = ((JSONObject) jLegs.get(j)).getJSONObject("distance");
                        HashMap<String, String> hmDistance = new HashMap();
                        hmDistance.put("distance", jDistance.getString("text"));
                        JSONObject jDuration = ((JSONObject) jLegs.get(j)).getJSONObject("duration");
                        HashMap<String, String> hmDuration = new HashMap();
                        hmDuration.put("duration", jDuration.getString("text"));
                        path.add(hmDistance);
                        path.add(hmDuration);
                        JSONArray jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");
                        for (int k = 0; k < jSteps.length(); k++) {
                            String polyline = "";
                            List<LatLng> list = decodePoly((String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points"));
                            for (int l = 0; l < list.size(); l++) {
                                HashMap<String, String> hm = new HashMap();
                                hm.put("lat", Double.toString((list.get(l)).latitude));
                                hm.put("lng", Double.toString((list.get(l)).longitude));
                                path.add(hm);
                            }
                        }
                        routes.add(path);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        private List<LatLng> decodePoly(String encoded) {
            List<LatLng> poly = new ArrayList();
            int index = 0;
            int len = encoded.length();
            int lat = 0;
            int lng = 0;
            while (index < len) {
                int index2;
                int shift = 0;
                int result = 0;
                int b = 0;
                while (true) {
                    index2 = index + 1;
                    b = encoded.charAt(index) - 63;
                    result |= (b & 31) << shift;
                    shift += 5;
                    if (b < 32) {
                        break;
                    }
                    index = index2;
                }
                lat += (result & 1) != 0 ? (result >> 1) ^ -1 : result >> 1;
                shift = 0;
                result = 0;
                index = index2;
                while (true) {
                    index2 = index + 1;
                    b = encoded.charAt(index) - 63;
                    result |= (b & 31) << shift;
                    shift += 5;
                    if (b < 32) {
                        break;
                    }
                    index = index2;
                }
                lng += (result & 1) != 0 ? (result >> 1) ^ -1 : result >> 1;
                poly.add(new LatLng(((double) lat) / 100000.0d, ((double) lng) / 100000.0d));
                index = index2;
            }
            return poly;
        }
    }
}
