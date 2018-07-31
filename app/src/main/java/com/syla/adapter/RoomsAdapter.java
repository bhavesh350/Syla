package com.syla.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.syla.MainActivity;
import com.syla.MyRoomsActivity;
import com.syla.R;
import com.syla.SavedRoomsActivity;
import com.syla.application.AppConstants;
import com.syla.application.MyApp;
import com.syla.models.Rooms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.syla.application.AppConstants.USER_ID;

/**
 * Created by Abhishek on 22-04-2017.
 */

public class RoomsAdapter extends RecyclerView.Adapter<RoomsAdapter.MyViewHolder> {

    List<Rooms> data;
    private LayoutInflater inflater;
    private Context context;
    private boolean isMine;
    private int dataCounter;

    public RoomsAdapter(Context context, List<Rooms> data, boolean isMine) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
        this.isMine = isMine;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_rooms, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Rooms r = data.get(position);
        holder.txt_name.setText(r.getRoomName());
        holder.txt_time.setText(MyApp.getDateOrTimeFromMillis(r.getRoomCreateTime()));
        holder.txt_members_count.setText(r.getCount() + " Members");

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_name, txt_members_count, txt_time;
        ImageButton btn_delete;
        ImageButton btn_share;

        public MyViewHolder(View itemView) {
            super(itemView);
            btn_share = itemView.findViewById(R.id.btn_share);
            txt_time = itemView.findViewById(R.id.txt_time);
            txt_name = itemView.findViewById(R.id.txt_name);
            txt_members_count = itemView.findViewById(R.id.txt_members_count);
            btn_delete = itemView.findViewById(R.id.btn_delete);
            btn_delete.setOnClickListener(this);
            itemView.setOnClickListener(this);
            btn_share.setOnClickListener(this);
            if (!isMine)
                btn_share.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onClick(View v) {
            if (v == btn_delete) {
                MyApp.spinnerStart(context, "Deleting...");
                if (isMine) {
                    ((MyRoomsActivity) context).db.collection("allRooms").document(data.get(getLayoutPosition()).getRoomId())
                            .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                               @Override
                                                               public void onSuccess(Void aVoid) {
                                                                   data.remove(getLayoutPosition());
                                                                   notifyDataSetChanged();
                                                                   MyApp.spinnerStop();
                                                               }
                                                           }
                    ).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            MyApp.spinnerStop();
                            Log.d("Logging", "Failed..." + e.getMessage());
                        }
                    });
                } else {
                    ((SavedRoomsActivity) context).db.collection("users").document(MyApp.getSharedPrefString(USER_ID))
                            .collection("savedRooms").document(data.get(getLayoutPosition()).getRoomId())
                            .delete().addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            MyApp.spinnerStop();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            data.remove(getLayoutPosition());
                            notifyDataSetChanged();
                            MyApp.spinnerStop();
                        }
                    });
                }
            } else if (v == itemView) {

                if (isMine) {
                    MyApp.spinnerStart(context, "Taking you to room...");
                    Map<String, Object> room = new HashMap<>();
                    room.put("roomName", data.get(getLayoutPosition()).getRoomName());
                    room.put("userName", data.get(getLayoutPosition()).getUserName());
                    room.put("isLeft", false);
                    room.put("isActive", true);
                    room.put("userId", MyApp.getSharedPrefString(AppConstants.USER_ID));
                    room.put("createTime", System.currentTimeMillis());
                    try {
                        room.put("lat", data.get(getLayoutPosition()).getLat());
                        room.put("lng", data.get(getLayoutPosition()).getLng());
                    } catch (Exception e) {
                        room.put("lat", 0.0);
                        room.put("lng", 0.0);
                    }
                    ((MyRoomsActivity) context).db.collection("allRooms").document(data.get(getLayoutPosition()).getRoomId())
                            .set(room).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            MyApp.spinnerStop();
                            String id = data.get(getLayoutPosition()).getRoomId();
                            MyApp.setSharedPrefString(AppConstants.CURRENT_ROOM_ID, id);
                            context.startActivity(new Intent(context, MainActivity.class).putExtra("isNew", false)
                                    .putExtra("isMine", true));
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            MyApp.spinnerStop();
                            MyApp.showMassage(context, "Some error occurred please try again.");
                        }
                    });
                } else {
                    CollectionReference user = ((SavedRoomsActivity) context).db.collection("allRooms")
                            .document(data.get(getLayoutPosition()).getRoomId()).collection("Users");
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
                    MyApp.spinnerStart(context, "Entering to room...");
                    ((SavedRoomsActivity) context).db.collection("allRooms")
                            .document(data.get(getLayoutPosition()).getRoomId())
                            .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {

                                if (dataCounter >= 7) {
                                    MyApp.popMessage("Alert!", "You cannot enter to the room as per max " +
                                            "number of user has been occupied.\nThank you", context);
                                    return;
                                }

                                Map<String, Object> usersMap = new HashMap<>();
                                usersMap.put("name", MyApp.getSharedPrefString(AppConstants.USER_NAME));
                                usersMap.put("isActive", true);
                                usersMap.put("isRemoved", false);
                                usersMap.put("isDeleted", false);
                                usersMap.put("isSaved", true);
                                try {
                                    usersMap.put("lat", 0.0);
                                    usersMap.put("lng", 0.0);
                                } catch (Exception e) {
                                    usersMap.put("lat", 0.0);
                                    usersMap.put("lng", 0.0);
                                }
                                usersMap.put("userId", MyApp.getSharedPrefString(AppConstants.USER_ID));
                                ((SavedRoomsActivity) context).db.collection("allRooms")
                                        .document(data.get(getLayoutPosition()).getRoomId())
                                        .collection("Users").document(MyApp.getSharedPrefString(AppConstants.USER_ID))
                                        .set(usersMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                MyApp.spinnerStop();
                                                MyApp.setSharedPrefString(AppConstants.CURRENT_ROOM_ID,
                                                        data.get(getLayoutPosition()).getRoomId());
                                                context.startActivity(new Intent(context, MainActivity.class));
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                MyApp.popMessage("Alert",
                                                        "There it seems have some problem in you room id " +
                                                                "please try again.\nThank you.", context);
                                                MyApp.spinnerStop();
                                            }
                                        });
                            } else {
                                MyApp.popMessage("Error", "This room does not exist anymore.", context);
                                MyApp.spinnerStop();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            MyApp.popMessage("Error", "This room does not exist anymore.", context);
                            MyApp.spinnerStop();
                        }
                    });
                }
            } else if (v == btn_share) {
                String link_val = data.get(getLayoutPosition()).getRoomId();
                String body = "Hi, I have created a room to share our location, so that we can track each other anytime" +
                        "\n'" + link_val
                        + "' is the room id you have to enter to join it.";
//                            String shareBody = "Hi, I have created a room to share our location, so that we can track each other anytime" +
//                                    "\n'" + currentRoomId + "' is the room id you have to enter to join it.";
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Join Room");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
                context.startActivity(Intent.createChooser(sharingIntent, "Share Via"));
            }
        }
    }
}
