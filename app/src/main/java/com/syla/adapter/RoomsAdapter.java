package com.syla.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.syla.MainActivity;
import com.syla.MyRoomsActivity;
import com.syla.R;
import com.syla.SavedRoomsActivity;
import com.syla.application.AppConstants;
import com.syla.application.MyApp;
import com.syla.models.Rooms;

import java.util.List;

import static com.syla.application.AppConstants.USER_ID;

/**
 * Created by Abhishek on 22-04-2017.
 */

public class RoomsAdapter extends RecyclerView.Adapter<RoomsAdapter.MyViewHolder> {

    List<Rooms> data;
    private LayoutInflater inflater;
    private Context context;
    private boolean isMine;

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
                MyApp.spinnerStart(context,"Deleting...");
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
                    ((SavedRoomsActivity)context).db.collection("users").document(MyApp.getSharedPrefString(USER_ID))
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
                MyApp.setSharedPrefString(AppConstants.CURRENT_ROOM_ID, data.get(getLayoutPosition()).getRoomId());
                context.startActivity(new Intent(context, MainActivity.class).putExtra("isMine", isMine));
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
