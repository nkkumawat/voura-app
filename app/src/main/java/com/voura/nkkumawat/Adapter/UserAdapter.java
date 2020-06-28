package com.voura.nkkumawat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.voura.nkkumawat.Models.User;
import com.voura.nkkumawat.R;
import com.voura.nkkumawat.VideoActivity;

import java.util.ArrayList;

public class UserAdapter extends BaseAdapter {
    Context context;
    ArrayList<User> users;
    public UserAdapter(Context context, ArrayList<User> users) {
        this.context = context;
        this.users = users;
    }
    @Override
    public int getCount() {
        return users.size();
    }
    @Override
    public Object getItem(int position) {
        return users.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.user_list, parent, false);
        }
        TextView tvUserName = convertView.findViewById(R.id.user_name_tv);
        final User user = (User) this.getItem(position);
        tvUserName.setText(user.username);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent videoActivity = new Intent(context, VideoActivity.class);
                videoActivity.putExtra("room", user.socketId);
                videoActivity.putExtra("to", user.socketId);
                videoActivity.putExtra("make-call", "true");
                context.startActivity(videoActivity);
            }
        });
        return convertView;
    }
}