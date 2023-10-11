package com.example.myinventory;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class AddShopActivity extends AppCompatActivity {
    TextInputLayout textInputEditText;
    ImageButton addShop;
    Button done;

    String shopName1;
    private FirebaseFirestore db;
    private SharedPreferences preferences;
    private String shopId;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!isConnected()){
            startActivity(new Intent(AddShopActivity.this, NoInternetActivity.class));
        }
        preferences = getSharedPreferences("pref",MODE_PRIVATE);
        setContentView(R.layout.activity_add_shop);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        textInputEditText = findViewById(R.id.shopName);
        addShop = findViewById(R.id.addShop);
        done = findViewById(R.id.done);
        db = FirebaseFirestore.getInstance();

        addShop.setEnabled(false);
        addShop.setBackgroundColor(getResources().getColor(R.color.disabled));

        addShop.setOnClickListener(v->{
            String shopName = textInputEditText.getEditText().getText().toString();
            if(!shopName.trim().isEmpty()){
                addShop(shopName);
            }
        });
        textInputEditText.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().equals("")){
                    addShop.setEnabled(false);
                    addShop.setBackgroundColor(getResources().getColor(R.color.disabled));
                }else{
                    addShop.setEnabled(true);
                    addShop.setBackgroundColor(getResources().getColor(R.color.teal_200));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        done.setOnClickListener(v->{
            db.collection("Shops").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if(queryDocumentSnapshots.size() != 0){
                        Intent i = new Intent(AddShopActivity.this,MainActivity.class);
                        Bundle b = new Bundle();
                        b.putString("shopId",shopId);
                        b.putString("shopName",shopName1);
                        i.putExtras(b);
                        startActivity(i);

//                        startActivity(new Intent(AddShopActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }else{
                        Toast.makeText(AddShopActivity.this, "Add at least one shop!", Toast.LENGTH_SHORT).show();
                    }
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("shopCount",queryDocumentSnapshots.size());
                    editor.commit();
                }
            });
        });



    }

    void addShop(String shopName){
        ProgressBar progressBar = findViewById(R.id.progress);
        progressBar.setVisibility(View.VISIBLE);

        Shop shop = new Shop(shopName);

        db.collection("Shops").add(shop).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                shopId = documentReference.getId();
                Toast.makeText(AddShopActivity.this, "Shop Created successfully", Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("shopId",documentReference.getId());
                editor.commit();
                shopName1 = shopName;
                textInputEditText.getEditText().setText("");
                progressBar.setVisibility(View.GONE);

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