package com.example.myinventory;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import com.example.myinventory.ui.home.Items;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddItemsFromExcelActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 1;
    private static final String LOG_TAG = "AndroidExample";

    private static final int pic_id = 123;
    String filePath;
    Button addExcel;
    private String shopId;
    private String shopName;
    FirebaseFirestore db;
    LinearLayout ll;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        db = FirebaseFirestore.getInstance();
        setContentView(R.layout.activity_add_items_from_excel);
        progressBar = findViewById(R.id.progress);
        ll = findViewById(R.id.ll);
        Bundle b = getIntent().getExtras();
        shopId = b.getString("shopId");
        shopName = b.getString("shopName");
        filePath = b.getString("path");
        addExcel = findViewById(R.id.addExcel);
        try {
            openExcel();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        addExcel.setOnClickListener( click ->{
            progressBar.setVisibility(View.VISIBLE);
            if(filePath!= null){
                FileInputStream fs = null;
                try {
                    fs = new FileInputStream(filePath);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
//Creating a workbook
                HSSFWorkbook workbook = null;
                try {
                    workbook = new HSSFWorkbook(fs);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Sheet sheet = workbook.getSheetAt(0);
                int rowTotal = sheet.getLastRowNum();

                for(int i = 0; i <= rowTotal; i++){
                    Row row = sheet.getRow(i);
                    Cell name = row.getCell(0);
                    Cell desc = row.getCell(1);
                    Cell qty = row.getCell(2);
                    Cell bought = row.getCell(3);
                    Cell sell = row.getCell(4);

                    addDataToFirestore(name+"",desc+"", (int) qty.getNumericCellValue(), (int) bought.getNumericCellValue(), (int) sell.getNumericCellValue());
                }
            }

        });
    }

    public void openExcel() throws IOException {
        if(filePath!= null){
            FileInputStream fs = new FileInputStream(filePath);
//Creating a workbook
            HSSFWorkbook workbook = new HSSFWorkbook(fs);
            Sheet sheet = workbook.getSheetAt(0);
            int rowTotal = sheet.getLastRowNum();

            for(int i = 0; i <= rowTotal; i++){
                Row row = sheet.getRow(i);
                Cell name = row.getCell(0);
                Cell desc = row.getCell(1);
                Cell qty = row.getCell(2);
                Cell bought = row.getCell(3);
                Cell sell = row.getCell(4);

                View view = getLayoutInflater().inflate(R.layout.table_view,null,false);
                TextView nameTV = view.findViewById(R.id.item);
                TextView descTV = view.findViewById(R.id.des);
                TextView qtyTV = view.findViewById(R.id.qty);
                TextView boughtTV = view.findViewById(R.id.bought);
                TextView soldTV = view.findViewById(R.id.sold);

                nameTV.setText(name+"");
                descTV.setText(desc+"");
                qtyTV.setText(qty+"");
                boughtTV.setText(bought+"");
                soldTV.setText(sell+"");

                ll.addView(view);
                System.out.println(name.getStringCellValue()+" ???????????????");
            }
        }

    }


    private void addDataToFirestore(String Name, String Description, int qty, int Bought, int Sell) {
        if(isConnected()){
            CollectionReference dbOrder = db.collection("Items");
            Items items  = new Items(Name, Description, qty, Bought, Sell,"",shopId);
            dbOrder.add(items).addOnSuccessListener(documentReference -> {
                Toast.makeText(AddItemsFromExcelActivity.this, "Items saved successfully", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                onBackPressed();
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddItemsFromExcelActivity.this, "Fail to add Item \n" + e, Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            Toast.makeText(AddItemsFromExcelActivity.this, "No Internet Connection!", Toast.LENGTH_SHORT).show();
        }

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