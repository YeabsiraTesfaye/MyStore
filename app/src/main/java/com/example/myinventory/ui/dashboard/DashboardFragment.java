package com.example.myinventory.ui.dashboard;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myinventory.R;
import com.example.myinventory.ui.home.Items;
import com.example.myinventory.ui.notifications.Transaction;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DashboardFragment extends Fragment {
    int dailyProfit, weaklyProfit, monthlyProfit, totalAsset, credit, totalSell;
    ArrayList<Items> items = new ArrayList<>();
    View v;
    TextView dailyProfitTV, weeklyProfitTV, monthlyProfitTV, expectedProfitTV, totalAssetTV, creditTV;
    LinearLayout ll;
    private FirebaseFirestore db;

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_dashboard2, container, false);
        dailyProfitTV = v.findViewById(R.id.daily);
        weeklyProfitTV = v.findViewById(R.id.weekly);
        monthlyProfitTV = v.findViewById(R.id.monthly);
        expectedProfitTV = v.findViewById(R.id.expectedAProfit);
        totalAssetTV = v.findViewById(R.id.totalAsset);
        creditTV = v.findViewById(R.id.credit);
        ll = v.findViewById(R.id.lowInStock);


        db = FirebaseFirestore.getInstance();
        // from date
        LocalDate currentdate = LocalDate.now();
        int currentDay = currentdate.getDayOfMonth();
        int currentMonth = currentdate.getMonthValue();
        int currentYear = currentdate.getYear();
        getTransaction(currentDay,currentMonth,currentYear);
        getItems();
        getWeeklySells();

        SwipeRefreshLayout swipeRefreshLayout = v.findViewById(R.id.refreshLayout);

        // Refresh  the layout
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {

                        LocalDate currentdate = LocalDate.now();
                        int currentDay = currentdate.getDayOfMonth();
                        int currentMonth = currentdate.getMonthValue();
                        int currentYear = currentdate.getYear();
                        getTransaction(currentDay,currentMonth,currentYear);
                        getItems();
                        getWeeklySells();

                        // This line is important as it explicitly
                        // refreshes only once
                        // If "true" it implicitly refreshes forever
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
        );

        return v;
    }

    void getTransaction(int day, int month, int year){
        db.collection("Transactions").orderBy("date").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot d : list) {
                        Transaction c = d.toObject(Transaction.class);
                        String item = c.getItem();
                        int quantity = c.getQuantity();
                        int bought = c.getBought();
                        int sold = c.getSold();
                        int total = c.getTotal();
                        int paid = c.getPaid();
                        int unpaid = c.getUnpaid();
                        Timestamp date = c.getDate();
                        credit += unpaid;
                        int Tmonth = date.toDate().getMonth()+1;
                        String[] split = date.toDate().toString().split(" ");
                        int Tyear = Integer.parseInt(split[split.length-1]);
                        if(month == Tmonth && year == Tyear && day == date.toDate().getDate()){
                            int profit = (sold*quantity) - (bought*quantity);
                            dailyProfit += profit;
                        }
                        if(month == Tmonth && year == Tyear){
                            int profit = (sold*quantity) - (bought*quantity);
                            monthlyProfit += profit;
                            System.out.println(profit+">>>>>>>>>>");
                        }
                    }
                    dailyProfitTV.setText(dailyProfit+" ETB");
                    monthlyProfitTV.setText(monthlyProfit+" ETB");

                    creditTV.setText(credit+" ETB");
                    dailyProfit = 0;
                    monthlyProfit = 0;
                    credit = 0;

                } else {
                    dailyProfitTV.setText(0+" ETB");
                    monthlyProfitTV.setText(0+" ETB");
                    creditTV.setText(0+" ETB");

                    Toast.makeText(getContext(), "No data found in Database", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Fail to get the data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    void getItems(){
        ll.removeAllViews();
        db.collection("Items").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                for (DocumentSnapshot d : list) {
                    View layout = getLayoutInflater().inflate(R.layout.image_view,null,false);
                    Items i = d.toObject(Items.class);
                    int boughtPrice = i.getBuy() * i.getQuantity();
                    if(i.getQuantity() <= 3){
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
                        Glide.with(getContext()).load(i.getUri()).into(iv);

                        ll.addView(layout);

                    }

                    totalAsset += boughtPrice;
                    totalSell += i.getSell()*i.getQuantity();

                }
                totalAssetTV.setText(totalAsset+" ETB");
                expectedProfitTV.setText(totalSell-totalAsset+" ETB");

                totalSell = 0;
                totalAsset = 0;
            }
        });
    }
    void getWeeklySells(){
        Timestamp now = Timestamp.now();
        db.collection("Transactions").orderBy("date").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot q: queryDocumentSnapshots){
                    Transaction t = q.toObject(Transaction.class);
                    int quantity = t.getQuantity();
                    int bought = t.getBought();
                    int sold = t.getSold();

                    long difference = Math.abs(now.toDate().getTime() - t.getDate().toDate().getTime());
                    if(difference <= 604800000){
                        Calendar c = Calendar.getInstance();
                        c.setTime(now.toDate());
                        int dayNum = c.get(Calendar.DAY_OF_WEEK);
                        Calendar c1 = Calendar.getInstance();
                        c1.setTime(t.getDate().toDate());
                        int dayNum1 = c1.get(Calendar.DAY_OF_WEEK);
                        if(dayNum >= dayNum1){
                            int profit = (sold*quantity) - (bought*quantity);
                            weaklyProfit+= profit;
                        }
                    }
                }
                weeklyProfitTV.setText(weaklyProfit+" ETB");
                weaklyProfit = 0;
            }
        });
    }
}