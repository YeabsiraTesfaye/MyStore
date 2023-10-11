package com.example.myinventory;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import com.demogorgorn.monthpicker.MonthPickerDialog;
import com.example.myinventory.databinding.ActivityAdvancedBinding;
import com.example.myinventory.ui.notifications.Transaction;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class AdvancedActivity extends AppCompatActivity {
    private static final boolean AUTO_HIDE = true;
    private static final int STORAGE_PERMISSION_CODE = 23;
    Button dayButton, monthButton, yearButton, clearFilter;
    LinearLayout dayLL, monthLL, yearLL, forGraph;
    ImageButton download;

    private ActivityAdvancedBinding binding;


//    ArrayList<BarEntry> entries = new ArrayList<>();
    private FirebaseFirestore db;
    List<DocumentSnapshot> list;
    ArrayList barEntriesArrayList;
    ScrollView dayWrapper,monthWrapper,yearWrapper;
    TextView dayInfo, monthInfo, yearInfo,shopNameTV;
    HashMap map = new HashMap();
    ArrayList barEntries;
    BarChart chart;
    ImageButton showHideChart;

    int monthTotalSell=0;

    private static final String EXCEL_SHEET_NAME = "Sheet1";
    private SharedPreferences preferences;
    private String shopId;
    private String shopName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!isConnected()){
            startActivity(new Intent(AdvancedActivity.this, NoInternetActivity.class));
        }

        preferences = getSharedPreferences("pref",MODE_PRIVATE);
        Bundle b = getIntent().getExtras();
        shopId = b.getString("shopId");
        shopName = b.getString("shopName");
        binding = ActivityAdvancedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        if (ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{WRITE_EXTERNAL_STORAGE}, 1);

//            return;
        }
        askForPermissions();
        map.put(1, "January");
        map.put(2, "February");
        map.put(3, "March");
        map.put(4, "April");
        map.put(5, "May");
        map.put(6, "June");
        map.put(7, "July");
        map.put(8, "August");
        map.put(9, "September");
        map.put(10, "October");
        map.put(11, "November");
        map.put(12, "December");

        dayButton = findViewById(R.id.day);
        monthButton = findViewById(R.id.month);
        yearButton = findViewById(R.id.year);
        clearFilter = findViewById(R.id.clear);

        dayLL = findViewById(R.id.showByDay);
        monthLL = findViewById(R.id.showByMonth);
        yearLL = findViewById(R.id.showByYear);
        forGraph = findViewById(R.id.forGraph);

        dayWrapper = findViewById(R.id.dayWrapper);
        monthWrapper = findViewById(R.id.monthWrapper);
        yearWrapper = findViewById(R.id.yearWrapper);

        shopNameTV = findViewById(R.id.shopName);
        System.out.println(shopName+" "+shopId);
        shopNameTV.setText(shopName);

        dayInfo = findViewById(R.id.dayInfo);
        monthInfo = findViewById(R.id.monthInfo);
        yearInfo = findViewById(R.id.yearInfo);

        download = findViewById(R.id.download);

        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        dayButton.setOnClickListener(view->{

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {

                            dayButton.setText(map.get(monthOfYear + 1) + "-" + dayOfMonth + "-" + year);
                            transactionFilter(dayOfMonth,monthOfYear+1,year);

                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        });

        monthButton.setOnClickListener(view->{
            AtomicInteger nowMonth= new AtomicInteger(mMonth);
            AtomicInteger nowYear= new AtomicInteger(mYear);
            MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(AdvancedActivity.this,
                    (selectedMonth, selectedYear) -> { // on date set
                    }, mYear, mMonth)    ;

            builder.setActivatedMonth(mMonth)
                    .setActivatedYear(mYear)
                    .setTitle("Select trading month")
                    .setMonthRange(Calendar.JANUARY, Calendar.DECEMBER)
                    .setOnMonthChangedListener(selectedMonth -> { // on month selected
                        nowMonth.set(selectedMonth + 1);
                        monthButton.setText(map.get(nowMonth.intValue()) + "-" + nowYear);
                        transactionFilter(0,nowMonth.intValue(),nowYear.intValue());

                    })
                    .setOnYearChangedListener(selectedYear -> {
                        nowYear.set(selectedYear);
                        monthButton.setText(map.get(nowMonth.intValue()) + "-" + nowYear);
                        transactionFilter(0,nowMonth.intValue(),nowYear.intValue());

                    })
                    .build()
                    .show();
        });

        yearButton.setOnClickListener(view->{
            MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(AdvancedActivity.this,
                    (selectedMonth, selectedYear) -> { // on date set
                    }, mYear, mMonth)    ;

            builder
                    .setActivatedYear(mYear)
                    .setTitle("Select trading year")
                    .showYearOnly()
                    .setOnYearChangedListener(selectedYear -> {
                        yearButton.setText(selectedYear+"");
                        transactionFilter(0,0,selectedYear);
                    })
                    .build()
                    .show();
        });

        clearFilter.setOnClickListener(view ->{
            yearWrapper.setVisibility(View.GONE);
            dayWrapper.setVisibility(View.GONE);
            monthWrapper.setVisibility(View.GONE);

            dayButton.setText("FILTER SELLS BY DATE");
            monthButton.setText("FILTER SELLS BY MONTH");
            yearButton.setText("FILTER SELLS BY YEAR");

            clearFilter.setVisibility(View.GONE);
            download.setVisibility(View.GONE);

            if(chart != null){

                chart.setVisibility(View.GONE);
                chart = null;
            }
        });

        barEntriesArrayList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
    }

    void transactionFilter(int day, int month, int year){
        ArrayList<Transaction> newList = new ArrayList<>();
        db.collection("Transactions").whereEqualTo("shopId",shopId).orderBy("date").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot d : list) {
                        Transaction c = d.toObject(Transaction.class);
                        int Tmonth = c.getDate().toDate().getMonth() + 1;
                        int Tyear = c.getDate().toDate().getYear()+1900;
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(c.getDate().toDate().getTime());
                        int Tday = cal.get(Calendar.DAY_OF_MONTH);


                         if (day != 0) {
                            if (day == Tday && month == Tmonth && year == Tyear) {
                                newList.add(c);
                            }
                        }else if(month != 0 && day == 0){
                            if (month == Tmonth && year == Tyear) {
                                newList.add(c);
                            }
                        } else if(month == 0 && day == 0){
                            if (year == Tyear) {
                                newList.add(c);
                            }
                        }
                    }
                    if(newList.size()>0){
                        clearFilter.setVisibility(View.VISIBLE);
                        download.setVisibility(View.VISIBLE);
                        if (day != 0) {
                            setNewList(newList,1,year);
                            dayInfo.setVisibility(View.GONE);
                            download.setOnClickListener(view ->{
                                createExcelWorkbook(newList,map.get(month)+" "+day+" "+year+"");
                            });
                        }else if(month != 0 && day == 0){
                            monthInfo.setVisibility(View.GONE);
                            setNewList(newList,2,year);
                            download.setOnClickListener(view ->{
                                createExcelWorkbook(newList,map.get(month)+" "+year+"");
                            });
                        } else if(month == 0 && day == 0){
                            yearInfo.setVisibility(View.GONE);
                            if(chart != null){

                                chart.setVisibility(View.GONE);
                                chart = null;
                            }
                            setNewList(newList,3, year);
                            download.setOnClickListener(view ->{
                                createExcelWorkbook(newList,year+"");
                            });

                        }
                    }else{
                        if (day != 0) {
                            dayInfo.setVisibility(View.VISIBLE);
                            dayWrapper.setVisibility(View.GONE);
                            dayInfo.setText("No Transaction on "+map.get(month)+" "+day+" "+year);
                        }else if(month != 0 && day == 0){
                            monthInfo.setVisibility(View.VISIBLE);
                            monthWrapper.setVisibility(View.GONE);

                            monthInfo.setText("No Transaction in "+map.get(month)+"-"+year);
                        } else if(month == 0 && day == 0){
                            yearInfo.setVisibility(View.VISIBLE);
                            yearWrapper.setVisibility(View.GONE);

                            yearInfo.setText("No Transaction in " +year);
                            if(chart != null){

                                chart.setVisibility(View.GONE);
                                chart = null;
                            }
                        }
                    }
                }else {
                    Toast.makeText(AdvancedActivity.this, "No data found in Database", Toast.LENGTH_SHORT).show();

                }
            }}).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AdvancedActivity.this, "Fail to get the data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void setNewList(ArrayList<Transaction> list, int type, int year){
        int totalSell=0;
        int totalPaid=0;
        int totalUnpaid=0;
        int totalProfit=0;
        if(type == 1){
            dayLL.removeAllViews();
        }else if(type == 2){
            monthLL.removeAllViews();
        }else if(type == 3){
            yearLL.removeAllViews();
        }

        int counter=0;
        for (Transaction c : list) {
            View view = getLayoutInflater().inflate(R.layout.table_view, binding.getRoot(), false);
            counter++;
            TextView itemTV = view.findViewById(R.id.item);
            TextView descTV = view.findViewById(R.id.des);
            TextView boughtTV = view.findViewById(R.id.bought);
            TextView soldTV = view.findViewById(R.id.sold);
            TextView qtyTV = view.findViewById(R.id.qty);
            TextView totalTV = view.findViewById(R.id.total);
            TextView soldByTV = view.findViewById(R.id.soldBy);
            TextView paidTV = view.findViewById(R.id.paid);
            TextView unpaidTV = view.findViewById(R.id.unpaid);
            TextView profitTV = view.findViewById(R.id.profit);
            TextView dateTV = view.findViewById(R.id.date);


            String item = c.getItem();
            String description = c.getDescription();
            int quantity = c.getQuantity();
            int bought = c.getBought();
            int sold = c.getSold();
            int total = c.getTotal();
            int paid = c.getPaid();
            int unpaid = c.getUnpaid();
            String soldBy = c.getSoldBy();
            Timestamp date = c.getDate();


            int Tmonth = date.toDate().getMonth()+1;
            String[] split = date.toDate().toString().split(" ");
            int Tyear = Integer.parseInt(split[split.length-1]);
                itemTV.setText(item);
                descTV.setText(description);
                qtyTV.setText(quantity+"");
                boughtTV.setText(bought+" ETB");
                soldTV.setText(sold+"ETB");
                paidTV.setText(paid+"ETB");
                unpaidTV.setText(unpaid+"ETB");
                soldByTV.setText(soldBy);
                totalTV.setText(total+" ETB");
                profitTV.setText(((quantity*sold)-(quantity*bought))+" ETB");
                dateTV.setText(date.toDate().toString());
                if(counter%2 == 0){
                    view.setBackgroundColor(Color.parseColor("#FFDADADA"));
                }else{
                    view.setBackgroundColor(Color.parseColor("#ffffff"));
                }
                if(type == 1){
                    dayLL.addView(view);
                }else if(type == 2){
                    monthLL.addView(view);
                }else if(type == 3){
                    yearLL.addView(view);
                }

            totalSell += (c.getQuantity()*c.getSold());
            totalPaid += (c.getPaid());
            totalUnpaid += (c.getUnpaid());
            totalProfit += ((quantity*sold)-(quantity*bought));
        }

        if(type == 1){
            if(chart != null){

                chart.setVisibility(View.GONE);
                chart = null;
            }
            TextView totalSold = findViewById(R.id.totalSold);
            TextView totalPaidTV = findViewById(R.id.totalPaid);
            TextView totalUnpaidTV = findViewById(R.id.totalUnpaid);
            TextView totalProfitTV = findViewById(R.id.totalProfit);
            dayWrapper.setVisibility(View.VISIBLE);
            monthWrapper.setVisibility(View.GONE);
            yearWrapper.setVisibility(View.GONE);
            monthInfo.setVisibility(View.GONE);
            yearInfo.setVisibility(View.GONE);

            monthButton.setText("FILTER SELLS BY MONTH");
            yearButton.setText("FILTER SELLS BY YEAR");
            totalSold.setText(totalSell+" ETB");
            totalPaidTV.setText(totalPaid+" ETB");
            totalUnpaidTV.setText(totalUnpaid+" ETB");
            totalProfitTV.setText(totalProfit+" ETB");
        }else if(type == 2){
            if(chart != null){

                chart.setVisibility(View.GONE);
                chart = null;
            }
            TextView totalSold = findViewById(R.id.totalSoldMonth);
            TextView totalPaidTV = findViewById(R.id.totalPaidMonth);
            TextView totalUnpaidTV = findViewById(R.id.totalUnpaidMonth);
            TextView totalProfitTV = findViewById(R.id.totalMonthProfit);
            monthWrapper.setVisibility(View.VISIBLE);
            dayWrapper.setVisibility(View.GONE);
            yearWrapper.setVisibility(View.GONE);
            dayInfo.setVisibility(View.GONE);
            yearInfo.setVisibility(View.GONE);

            dayButton.setText("FILTER SELLS BY DATE");
            yearButton.setText("FILTER SELLS BY YEAR");
            totalSold.setText(totalSell+" ETB");
            totalPaidTV.setText(totalPaid+" ETB");
            totalUnpaidTV.setText(totalUnpaid+" ETB");
            totalProfitTV.setText(totalProfit+" ETB");
        }else if(type == 3){
            TextView totalSold = findViewById(R.id.totalSoldYear);
            TextView totalPaidTV = findViewById(R.id.totalPaidYear);
            TextView totalUnpaidTV = findViewById(R.id.totalUnpaidYear);
            TextView totalProfitTV = findViewById(R.id.totalYearProfit);

            yearWrapper.setVisibility(View.VISIBLE);
            dayWrapper.setVisibility(View.GONE);
            monthWrapper.setVisibility(View.GONE);
            dayInfo.setVisibility(View.GONE);
            monthInfo.setVisibility(View.GONE);

            dayButton.setText("FILTER SELLS BY DATE");
            monthButton.setText("FILTER SELLS BY MONTH");
            totalSold.setText(totalSell+" ETB");
            totalPaidTV.setText(totalPaid+" ETB");
            totalUnpaidTV.setText(totalUnpaid+" ETB");
            totalProfitTV.setText(totalProfit+" ETB");

            getEntries(year);

        }

    }

    private void getEntries(int year) {
        barEntries = new ArrayList<>();
        for (int i = 1; i <= 12; i++){
            getMonthTotalSell(i,year);
        }
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                BarDataSet dataset = new BarDataSet(barEntries, "");
                ArrayList<String> labels = new ArrayList<String>();
                labels.add("January");
                labels.add("February");
                labels.add("March");
                labels.add("April");
                labels.add("May");
                labels.add("June");
                labels.add("July");
                labels.add("August");
                labels.add("September");
                labels.add("October");
                labels.add("November");
                labels.add("December");
                chart = new BarChart(AdvancedActivity.this);
                showHideChart = new ImageButton(AdvancedActivity.this);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                showHideChart.setLayoutParams(params);
                showHideChart.setImageDrawable(getResources().getDrawable(R.drawable.baseline_arrow_circle_down_24));


                showHideChart.setOnClickListener(click->{
                    if(chart.getVisibility() == View.VISIBLE){
                        chart.setVisibility(View.GONE);
                        showHideChart.setImageDrawable(getResources().getDrawable(R.drawable.baseline_arrow_circle_up_24));

                    }else{
                        chart.setVisibility(View.VISIBLE);
                        showHideChart.setImageDrawable(getResources().getDrawable(R.drawable.baseline_arrow_circle_down_24));
                    }
                });

                XAxis xAxis = chart.getXAxis();
                xAxis.setGranularity(2f);
                xAxis.setGranularityEnabled(true);
                xAxis.setCenterAxisLabels(true);
                xAxis.setDrawGridLines(true);

                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                chart.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 700));
                dataset.setColors(ColorTemplate.MATERIAL_COLORS);
                BarData data = new BarData(dataset);

                data.setValueTextSize(10f);
                chart.setData(data);
                forGraph.removeAllViews();
                forGraph.addView(showHideChart);
                forGraph.addView(chart);
            }
        };
        handler.postDelayed(runnable, 1000);
    }
    void getMonthTotalSell(int month, int year){
        db.collection("Transactions").whereEqualTo("shopId",shopId).orderBy("date").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot d : list) {
                        Transaction c = d.toObject(Transaction.class);
                        int Tmonth = c.getDate().toDate().getMonth()+1;
                        String[] split = c.getDate().toDate().toString().split(" ");
                        int Tyear = Integer.parseInt(split[split.length-1]);


                        if(month == Tmonth && year == Tyear){
                            String item = c.getItem();
                            String description = c.getDescription();
                            int quantity = c.getQuantity();
                            int bought = c.getBought();
                            int sold = c.getSold();
                            String soldby = c.getSoldBy();
                            int total = c.getTotal();
                            monthTotalSell = ((sold*quantity)-(bought*quantity))+ monthTotalSell;
                        }

                    }
                    barEntries.add(new BarEntry(month, monthTotalSell));
                    monthTotalSell = 0;

                } else {

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }


    public void createExcelWorkbook(List<Transaction> transactions,String time) {
        ExportExcel ee = new ExportExcel();
//        if(ee. exportDataIntoWorkbook(AdvancedActivity.this,time,transactions)){
//            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
//        }
        ee. simpleAdd(AdvancedActivity.this,transactions,time);
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {

            }
        }
    }

    public void askForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }

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