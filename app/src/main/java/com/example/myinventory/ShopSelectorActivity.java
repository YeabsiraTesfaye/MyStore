package com.example.myinventory;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ShopSelectorActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ArrayList<String> shops;
    LinearLayout ll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_selector);
        if(!isConnected()){
            startActivity(new Intent(ShopSelectorActivity.this, NoInternetActivity.class));
        }
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        db = FirebaseFirestore.getInstance();
        ll = findViewById(R.id.ll);
        shops = new ArrayList<>();


        db.collection("Shops").orderBy("name").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots.size() == 0){
                    startActivity(new Intent(ShopSelectorActivity.this, AddShopActivity.class));
                }
                for (QueryDocumentSnapshot q : queryDocumentSnapshots) {

                    Shop shop = q.toObject(Shop.class);
//                    shops.add(shop.getName());
                    View v = View.inflate(ShopSelectorActivity.this,R.layout.spinner_list2,null);
                    TextView shopName = v.findViewById(R.id.shopName);
                    shopName.setText(shop.getName());
                    v.setOnClickListener(click->{
                        Intent i = new Intent(ShopSelectorActivity.this,MainActivity.class);
                        Bundle b = new Bundle();
                        b.putString("shopId",q.getId());
                        b.putString("shopName",shop.getName());
                        i.putExtras(b);
                        startActivity(i);
                    });
                    ll.addView(v);

                    SharedPreferences sh = getSharedPreferences("pref",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sh.edit();
                    editor.putInt("shopCount", queryDocumentSnapshots.size());
                    editor.commit();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("here");
                startActivity(new Intent(ShopSelectorActivity.this, AddShopActivity.class));
            }
        });
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