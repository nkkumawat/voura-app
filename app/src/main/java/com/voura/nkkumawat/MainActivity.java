package com.voura.nkkumawat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.voura.nkkumawat.Adapter.UserAdapter;
import com.voura.nkkumawat.Models.User;
import com.voura.nkkumawat.Utils.SocketHelper;
import com.voura.nkkumawat.Utils.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    final int PERMISSIONS_CODE = 141;
    DrawerLayout drawer;
    ArrayList<User> allUsers = new ArrayList<>();
    NavigationView navigationView;
    private String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    UserAdapter adapter;
    ListView userListView;
    SharedPreferences sharedpreferences;
    public static final String USER_PREFERENCES = "voura_user" ;
    public String username = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        setUser();
        checkPermissions();
    }

    public void setUser() {
        sharedpreferences = getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
        String name = sharedpreferences.getString("user_name", "");
        if(name.equalsIgnoreCase("")){
            username = "voura_user_" + Util.getRandomString();
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString("user_name", username);
            editor.apply();
        } else {
          username = name;
        }
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = (TextView) headerView.findViewById(R.id.username);
        navUsername.setText(username);
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_CODE);
        } else {
            startApp();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_CODE
                && grantResults.length == 2
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            startApp();
        } else {
            finish();
        }
    }

    private void startApp() {
        userListView = (ListView) findViewById(R.id.user_list_lv);
        JSONObject data = new JSONObject();
        try {
            data.put("username", username);
        } catch (Exception e ) {
            e.printStackTrace();
        }
        SocketHelper.socket.emit("get-active-users", data);
        SocketHelper.socket.on("add-user-list", response -> {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject data = (JSONObject) response[0];
                        JSONArray users = (JSONArray) data.getJSONArray("users");
                        for(int i = 0 ; i < users.length(); i ++) {
                            JSONObject u = (JSONObject)  users.getJSONObject(i);
                            allUsers.add(new User(u.get("username").toString(), u.get("socketId").toString()));
                        }
                        adapter = new UserAdapter(MainActivity.this, allUsers);
                        userListView.setAdapter(adapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
        SocketHelper.socket.on("remove-user-list", response -> {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject data = (JSONObject) response[0];
                        JSONArray users = (JSONArray) data.getJSONArray("users") ;
                        for(int i = 0 ; i < users.length(); i ++) {
                            User u = new User(users.get(i).toString(), users.get(i).toString());
                            Iterator itr = allUsers.iterator();
                            while (itr.hasNext()) {
                                User x = (User)itr.next();
                                if (x.socketId.equals(u.socketId)) itr.remove();
                            }
                        }
                        adapter = new UserAdapter(MainActivity.this, allUsers);
                        userListView.setAdapter(adapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
        SocketHelper.socket.on("call-made", response -> {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Receiving call from ......");
                        JSONObject obj = (JSONObject) response[0];
                        String socketId = obj.getString("from");
                        String room = obj.getString("room");
                        builder.setPositiveButton("Answer", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                JSONObject object = new JSONObject();
                                try {
                                    object.put("to", socketId);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                SocketHelper.socket.emit("make-answer", object);
                                Intent videoActivity = new Intent(MainActivity.this, VideoActivity.class);
                                videoActivity.putExtra("room", room);
                                MainActivity.this.startActivity(videoActivity);
                            }
                        });
                        builder.setMessage(socketId);
                        AlertDialog dialog = builder.create();
                        dialog.setCancelable(false);
                        dialog.show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        });

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
    public boolean onNavigationItemSelected(MenuItem item) {
        return true;
    }
}
