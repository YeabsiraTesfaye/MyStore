package com.example.myinventory;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

public class AvailableShops extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(!isConnected()){
            startActivity(new Intent(AvailableShops.this, NoInternetActivity.class));
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_shops);
    }
    public boolean isConnected() {
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            return connected;
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
        return connected;
    }

}