package com.syla;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class SavedRoomsActivity extends CustomActivity {

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
        db.collection("users").document(MyApp.getSharedPrefString(AppConstants.USER_ID)).collection("savedRooms")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<QuerySnapshot> task) {
                if (task.getResult().size() == 0) {
                    MyApp.popFinishableMessage("Syla Message", "It seems you are new here, " +
                            "because there is no room created by you yet. Please create them and try again." +
                            "\nThank you.", SavedRoomsActivity.this
                    );
                }
                if (task.isSuccessful()) {
                    for (int i = 0; i < task.getResult().size(); i++) {
                        final int position = i;
                        final Rooms r = new Rooms();

                        DocumentSnapshot doc = task.getResult().getDocuments().get(i);
                        db.collection("allRooms").document(doc.getId()).get().addOnSuccessListener(
                                new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot doc) {
                                        try {
                                            r.setRoomId(doc.getId());
                                            r.setRoomCreateTime(doc.getLong("createTime"));
                                            r.setRoomName(doc.getString("roomName"));

                                            doc.getReference().collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    count = task.getResult().size();
                                                    Log.d("Logging", "my room size is " + count);
                                                    r.setCount(count);
                                                    adapter.notifyDataSetChanged();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d("Logging", "failed " + e.getMessage());
                                                }
                                            });
                                            r.setCount(count);
                                            count = 0;
                                            if (!doc.getBoolean("isLeft"))
                                                myRooms.add(r);
                                            Log.d("debug", "position is " + position + " & size check is " + (task.getResult().size() - 1));
                                            if (position == (task.getResult().size() - 1)) {
                                                Log.d("debug", "came to true");
                                                Log.d("debug", "myRoom size is " + myRooms.size());
                                                if (myRooms.size() == 0) {
                                                    Log.d("debug", "satya vachan");
                                                    MyApp.popMessage("Syla", "No room has been saved yet.", getContext());
                                                }
                                            }
                                        } catch (Exception e) {
                                            if (myRooms.size() == 0) {
                                                MyApp.popFinishableMessage("Syla",
                                                        "No room has been saved yet.", SavedRoomsActivity.this);
                                            }
                                            e.printStackTrace();
                                        }
                                    }
                                }
                        ).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                MyApp.spinnerStop();
                                MyApp.popMessage("Syla", "No room has been saved yet.", getContext());
                            }
                        });
                    }

                    adapter = new RoomsAdapter(getContext(), myRooms, false);
                    rv_list.setAdapter(adapter);
                    MyApp.spinnerStop();

//                    if (myRooms.size() == 0) {
//                        MyApp.popFinishableMessage("Syla Message", "You do not have any saved room yet.", SavedRoomsActivity.this);
//                    }
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
        return SavedRoomsActivity.this;
    }
}
