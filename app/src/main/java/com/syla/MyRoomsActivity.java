package com.syla;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.syla.adapter.RoomsAdapter;
import com.syla.application.AppConstants;
import com.syla.application.MyApp;
import com.syla.models.Rooms;

import java.util.ArrayList;
import java.util.List;

public class MyRoomsActivity extends CustomActivity {

    private RecyclerView rv_list;
    public FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RoomsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_rooms);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getIntent().getStringExtra("title"));
        }

        setupViews();

    }

    private List<Rooms> myRooms = new ArrayList<>();
    int count = 0;

    private void setupViews() {
        rv_list = findViewById(R.id.rv_list);
        rv_list.setLayoutManager(new LinearLayoutManager(getContext()));
        MyApp.spinnerStart(getContext(), "Getting your rooms...");
        db.collection("allRooms").whereEqualTo("userId", MyApp.getSharedPrefString(AppConstants.USER_ID))
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.getResult().size() == 0) {
                    MyApp.popFinishableMessage("Syla Message", "It seems you are new here, " +
                            "because there is no room created by you yet. Please create them and try again." +
                            "\nThank you.", MyRoomsActivity.this
                    );
                }
                if (task.isSuccessful()) {
                    for (int i = 0; i < task.getResult().size(); i++) {
                        final Rooms r = new Rooms();

                        DocumentSnapshot doc = task.getResult().getDocuments().get(i);
                        r.setRoomId(doc.getId());
                        r.setRoomCreateTime(doc.getLong("createTime"));
                        r.setRoomName(doc.getString("roomName"));

//                        List<Rooms.Users> user = new ArrayList<>();
                        doc.getReference().collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                count = task.getResult().size();
                                Log.d("Logging", "my room size is " + count);
                                r.setCount(count);
                                adapter.notifyDataSetChanged();
//                                Rooms.Users u = new Rooms().new Users();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("Logging", "failed " + e.getMessage());
                            }
                        });
                        r.setCount(count);
                        count = 0;
                        myRooms.add(r);
                    }

                    adapter = new RoomsAdapter(getContext(), myRooms, true);
                    rv_list.setAdapter(adapter);
                    MyApp.spinnerStop();
                } else {
                    MyApp.spinnerStop();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                MyApp.spinnerStop();
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drawer, menu);
        return true;
    }


    private Context getContext() {
        return MyRoomsActivity.this;
    }
}
