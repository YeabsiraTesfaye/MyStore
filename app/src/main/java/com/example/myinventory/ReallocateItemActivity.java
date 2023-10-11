package com.example.myinventory;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myinventory.ui.credits.Credit;
import com.example.myinventory.ui.home.Items;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ReallocateItemActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    String shopName, shopId;
    LinearLayout ll;
    private SharedPreferences preferences;
    ArrayList<String> shops = new ArrayList<>();
    HashMap<String,String> nameToId = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reallocate_item);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        db = FirebaseFirestore.getInstance();
        preferences = getSharedPreferences("pref",MODE_PRIVATE);
        Bundle b = getIntent().getExtras();
        shopId = b.getString("shopId");
        shopName = b.getString("shopName");
        db.collection("Shops").whereNotEqualTo("name",shopName).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot q :
                        queryDocumentSnapshots) {
                    Shop shop = q.toObject(Shop.class);
                    shops.add(shop.getName());
                    nameToId.put(shop.getName(),q.getId());
                }
            }
        });
        ll = findViewById(R.id.ll);
        getItems();
    }
    void getItems(){
        ll.removeAllViews();
        db.collection("Items").whereEqualTo("shopId",shopId).orderBy("name").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                for (DocumentSnapshot d : list) {
                    View layout = getLayoutInflater().inflate(R.layout.image_view,null,false);
                    Items i = d.toObject(Items.class);

                    if(i.getQuantity() > 0){
                        TextView nameTV = layout.findViewById(R.id.name);
                        TextView descriptionTV = layout.findViewById(R.id.description);
                        TextView priveTV = layout.findViewById(R.id.price);
                        TextView quantityTV = layout.findViewById(R.id.quantity);
                        ImageView iv = layout.findViewById(R.id.idIVItem);
                        Button sell = layout.findViewById(R.id.sellBtn);

                        nameTV.setText(i.getName());
                        descriptionTV.setText(i.getDescription());
                        priveTV.setVisibility(View.GONE);
                        sell.setVisibility(View.GONE);
                        quantityTV.setText(i.getQuantity()+" in Stock");
                        if(i.getUri().trim() == ""){
                            Glide.with(ReallocateItemActivity.this).load(R.drawable.image).into(iv);
                        }else{
                            Glide.with(ReallocateItemActivity.this).load(i.getUri()).into(iv);
                        }

                        layout.setOnClickListener(click->{

                            LayoutInflater inflater = getLayoutInflater();
                            View alertLayout = inflater.inflate(R.layout.reallocate_layout, null);
                            final TextView from = (TextView) alertLayout.findViewById(R.id.from);
                            final NumberPicker quantity = alertLayout.findViewById(R.id.quantityPicker);
                            final Spinner to = (Spinner) alertLayout.findViewById(R.id.to);

                            from.setText(shopName);
                            quantity.setMaxValue(i.getQuantity());

                            @SuppressLint({"NewApi", "LocalSuppress"}) final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                                    Objects.requireNonNull(ReallocateItemActivity.this),R.layout.spinner_list,shops);

                            spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_list);
                            to.setAdapter(spinnerArrayAdapter);





                            AlertDialog.Builder alert = new AlertDialog.Builder(ReallocateItemActivity.this);
//                            alert.setIcon(getActivity().getResources().getDrawable(R.drawable.addnote));
                            alert.setView(alertLayout);
                            alert.setCancelable(false);
                            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(ReallocateItemActivity.this.getBaseContext(), "Cancel clicked", Toast.LENGTH_SHORT).show();


                                }
                            });

                            final String[] toShop = {""};
                            to.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    toShop[0] = shops.get(position);

                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });

                            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                        int oldQuantity = i.getQuantity();
                                        i.setQuantity(oldQuantity-quantity.getValue());
                                        db.collection("Items").document(d.getId()).set(i).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                i.setShopId(nameToId.get(toShop[0]));
                                                i.setQuantity(quantity.getValue());
                                                db.collection("Items").add(i).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        quantityTV.setText(oldQuantity-quantity.getValue());
                                                        if(oldQuantity-quantity.getValue() == 0){
                                                            ll.removeView(layout);
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                }
                            });
                            AlertDialog dialog = alert.create();
                            dialog.show();

                        });
                        ll.addView(layout);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println(e +" exception two");
            }
        });
    }
    void update(String id, int amount){

    }
}