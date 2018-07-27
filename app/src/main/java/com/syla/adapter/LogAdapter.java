package com.syla.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.syla.MainActivity;
import com.syla.R;
import com.syla.application.AppConstants;
import com.syla.application.MyApp;
import com.syla.models.Rooms;
import com.syla.models.Users;

import java.util.List;

/**
 * Created by Abhishek on 22-04-2017.
 */

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.MyViewHolder> {

    List<Users> data;
    private LayoutInflater inflater;
    private Context context;
    private boolean isAdmin;

    public LogAdapter(Context context, List<Users> data, boolean isAdmin) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.isAdmin = isAdmin;
        this.data = data;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_log, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Users u = data.get(position);

        if (u.isActive()) {
            holder.txt_value.setText(u.getName());
        } else {
            holder.txt_value.setText(u.getName() + " (Inactive)");
        }

        if (u.isAdmin()) {
            holder.txt_value.setText(u.getName() + " (Admin)");
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_value;
        ImageButton btn_direction;
        ImageButton btn_delete;
        ImageButton btn_map;

        public MyViewHolder(View itemView) {
            super(itemView);
            txt_value = itemView.findViewById(R.id.txt_value);
            btn_direction = itemView.findViewById(R.id.btn_direction);
            btn_delete = itemView.findViewById(R.id.btn_delete);
            btn_map = itemView.findViewById(R.id.btn_map);
            btn_direction.setOnClickListener(this);
            btn_delete.setOnClickListener(this);
            btn_map.setOnClickListener(this);
            if (!isAdmin || MyApp.getStatus(AppConstants.IS_GUEST)) {
                btn_delete.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View v) {
            if (v == btn_direction) {
                ((MainActivity) context).goToUser(data.get(getLayoutPosition()));
            } else if (v == btn_delete) {
                AlertDialog.Builder b = new AlertDialog.Builder(context);
                b.setMessage("Are you sure to delete this user from your room?").setTitle("Delete?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                ((MainActivity) context).deleteUser(data.get(getLayoutPosition()));
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
            } else if (v == btn_map) {
                ((MainActivity) context).goToGoogleMap(data.get(getLayoutPosition()));
            }
        }
    }
}
