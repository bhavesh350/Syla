package com.syla;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.syla.application.AppConstants;
import com.syla.application.MyApp;
import com.syla.utils.LocationProvider;

import java.util.HashMap;
import java.util.Map;

import static com.syla.application.AppConstants.IS_GUEST;

public class DrawerActivity extends CustomActivity
        implements NavigationView.OnNavigationItemSelectedListener, LocationProvider.LocationCallback, LocationProvider.PermissionCallback {
    private static final int PERMISSION_ACCESS_COARSE_LOCATION = 11;
    private Button btn_create_room;
    private Button btn_join_room;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private LocationProvider provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        mAuth = FirebaseAuth.getInstance();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        provider = new LocationProvider(getContext(), this, this);
        provider.connect();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(" HOME");
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
        setTranslucentStatusBar(getWindow());
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_ACCESS_COARSE_LOCATION);
        }

        setupViews();


        if (MyApp.getStatus(IS_GUEST)) {
            Menu nav_Menu = navigationView.getMenu();
            nav_Menu.findItem(R.id.nav_my_rooms).setVisible(false);
            nav_Menu.findItem(R.id.nav_saved_rooms).setVisible(false);
            nav_Menu.findItem(R.id.nav_logout).setTitle("Login");
        }
    }


    private void setupViews() {
        btn_create_room = findViewById(R.id.btn_create_room);
        btn_join_room = findViewById(R.id.btn_join_room);

        setTouchNClick(R.id.btn_join_room);
        setTouchNClick(R.id.btn_create_room);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (!MyApp.isLocationEnabled(DrawerActivity.this)) {
            AlertDialog.Builder b = new AlertDialog.Builder(getContext());
            b.setMessage("To access BLE device please enable GPS");
            b.setTitle("Enable GPS");
            b.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent enableLocationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(enableLocationIntent, 10);
                }
            }).create().show();
            return;
        }
        if (v == btn_join_room) {
            joinRoomDialog();
        } else if (v == btn_create_room) {
            createRoomDialog();
        }
    }

    private void createRoomDialog() {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00ffffff")));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_create_room);

        Button btn_come_in = dialog.findViewById(R.id.btn_come_in);
        ImageButton btn_close = dialog.findViewById(R.id.btn_close);
        final EditText edt_room_name = dialog.findViewById(R.id.edt_room_name);
        final EditText edt_user_name = dialog.findViewById(R.id.edt_user_name);

        if (MyApp.getStatus(AppConstants.IS_LOGIN)) {
            edt_user_name.setText(MyApp.getSharedPrefString(AppConstants.USER_NAME));
        }

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btn_come_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edt_room_name.getText().toString().isEmpty() || edt_user_name.getText().toString().isEmpty()) {
                    MyApp.showMassage(getContext(), "Please fill both the fields and try again.");
                    return;
                }
                MyApp.spinnerStart(getContext(), "Creating room...");
                dialog.dismiss();
                Map<String, Object> room = new HashMap<>();
                room.put("roomName", edt_room_name.getText().toString());
                room.put("userName", edt_user_name.getText().toString());
                room.put("isLeft", false);
                room.put("userId", MyApp.getSharedPrefString(AppConstants.USER_ID));
                room.put("createTime", System.currentTimeMillis());
                try {
                    room.put("lat", location.getLatitude());
                    room.put("lng", location.getLongitude());
                } catch (Exception e) {
                    room.put("lat", 0.0);
                    room.put("lng", 0.0);
                }
                db.collection("allRooms").add(room)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                MyApp.spinnerStop();
                                String id = documentReference.getId();
                                MyApp.setSharedPrefString(AppConstants.CURRENT_ROOM_ID, id);
                                startActivity(new Intent(getContext(), MainActivity.class).putExtra("isNew", true)
                                        .putExtra("isMine", true));
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        MyApp.spinnerStop();
                        MyApp.showMassage(getContext(), "Some error occurred please try again.");
                    }
                });


            }
        });

        dialog.show();

    }

    private int dataCounter = 0;

    private void joinRoomDialog() {

        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00ffffff")));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_join_room);

        Button btn_come_in = dialog.findViewById(R.id.btn_come_in);
        ImageButton btn_close = dialog.findViewById(R.id.btn_close);
        final EditText edt_user_name = dialog.findViewById(R.id.edt_user_name);
        final EditText edt_room_link = dialog.findViewById(R.id.edt_room_link);
        edt_user_name.setText(MyApp.getSharedPrefString(AppConstants.USER_NAME));
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });

        btn_come_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CollectionReference user = db.collection("allRooms").document("tfRmitP92ywSv8ETF2vN").collection("Users");
                user.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        QuerySnapshot doc = task.getResult();
                        dataCounter = doc.size();
                        Log.d("Logging", "Size is " + doc.size());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Logging", "Failed to read");
                    }
                });
                MyApp.spinnerStart(getContext(), "Entering to room...");
                db.collection("allRooms").document(edt_room_link.getText().toString())
                        .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            if (edt_user_name.getText().toString().isEmpty() || edt_room_link.getText().toString().isEmpty()) {
                                MyApp.showMassage(getContext(), "Please fill both fields and try again.");
                                return;
                            }

                            if (dataCounter >= 7) {
                                dialog.dismiss();
                                MyApp.popMessage("Alert!", "You cannot enter to the room as per max number of user has been occupied.\nThank you", getContext());
                                return;
                            }


                            dialog.dismiss();


                            Map<String, Object> usersMap = new HashMap<>();
                            usersMap.put("name", edt_user_name.getText().toString());
                            usersMap.put("isActive", true);
                            usersMap.put("isRemoved", false);
                            usersMap.put("isDeleted", false);
                            usersMap.put("isSaved", false);
                            try {
                                usersMap.put("lat", location.getLatitude());
                                usersMap.put("lng", location.getLongitude());
                            } catch (Exception e) {
                                usersMap.put("lat", 0.0);
                                usersMap.put("lng", 0.0);
                            }
                            usersMap.put("userId", MyApp.getSharedPrefString(AppConstants.USER_ID));
                            db.collection("allRooms").document(edt_room_link.getText().toString()/*edt_room_link.getText().toString()*/)
                                    .collection("Users").document(MyApp.getSharedPrefString(AppConstants.USER_ID)).set(usersMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            MyApp.spinnerStop();
                                            MyApp.setSharedPrefString(AppConstants.CURRENT_ROOM_ID, edt_room_link.getText().toString());
                                            startActivity(new Intent(getContext(), MainActivity.class));
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            MyApp.popMessage("Alert", "There it seems have some problem in you room id please re-enter and try again.\nThank you.", getContext());
                                            MyApp.spinnerStop();
                                        }
                                    });
                        } else {
                            MyApp.popMessage("Error", "Room Id you have entered is wrong, please enter a correct room id and try again.\nThank you", getContext());
                            MyApp.spinnerStop();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        MyApp.popMessage("Error", "Room Id you have entered is wrong, please enter a correct room id and try again.\nThank you", getContext());
                        MyApp.spinnerStop();
                    }
                });
            }
        });

        dialog.show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drawer, menu);
        return true;
    }


//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
//
        if (id == R.id.nav_create_room) {
            createRoomDialog();
        } else if (id == R.id.nav_join_room) {
            joinRoomDialog();
        } else if (id == R.id.nav_my_rooms) {
            startActivity(new Intent(getContext(), MyRoomsActivity.class).putExtra("title", "My Rooms"));
        } else if (id == R.id.nav_saved_rooms) {
            startActivity(new Intent(getContext(), MyRoomsActivity.class).putExtra("title", "Saved Rooms"));
        } else if (id == R.id.nav_logout) {
            try {
                mAuth.signOut();
            } catch (Exception e) {
            }
            MyApp.setStatus(AppConstants.IS_LOGIN, false);
            MyApp.setSharedPrefString(AppConstants.USER_ID, "");
            startActivity(new Intent(getContext(), SocialLoginActivity.class));
            finishAffinity();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static void setTranslucentStatusBar(Window window) {
        if (window == null) return;
        int sdkInt = Build.VERSION.SDK_INT;
        if (sdkInt >= Build.VERSION_CODES.LOLLIPOP) {
            setTranslucentStatusBarLollipop(window);
        } else if (sdkInt >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatusBarKiKat(window);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void setTranslucentStatusBarLollipop(Window window) {
        window.setStatusBarColor(
                window.getContext()
                        .getResources()
                        .getColor(R.color.transparent));
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static void setTranslucentStatusBarKiKat(Window window) {
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    provider = new LocationProvider(getContext(), DrawerActivity.this, DrawerActivity.this);
                    provider.connect();
                    // All good!
                } else {
                    Toast.makeText(this, "Need your location!", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    private Context getContext() {
        return DrawerActivity.this;
    }

    private Location location;

    @Override
    public void handleNewLocation(Location location) {
        this.location = location;
    }

    @Override
    public void handleManualPermission() {

    }

}
