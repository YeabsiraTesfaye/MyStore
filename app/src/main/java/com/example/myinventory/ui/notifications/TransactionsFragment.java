package com.example.myinventory.ui.notifications;

import static android.content.Context.MODE_PRIVATE;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.demogorgorn.monthpicker.MonthPickerDialog;
import com.example.myinventory.MainActivity;
import com.example.myinventory.R;
import com.example.myinventory.databinding.FragmentNotificationsBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class TransactionsFragment extends Fragment {
    int year, month = 0;
    LinearLayout ll;
    private FirebaseFirestore db;
    ExtendedFloatingActionButton fab;
    FragmentNotificationsBinding binding;
    TextView textView;
    NestedScrollView nsv;
    LinearLayout title;
    ImageButton cancel;
    HashMap map = new HashMap();
    int totalSell = 0;
    int totalBought = 0;
    int totalPaid = 0;
    int totalUnpaid = 0;
    int totalProfit = 0;
    LayoutInflater inflater;
    SharedPreferences preferences;
    String shopId;
    private String shopName;
    View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        preferences = this.getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("refresh", true);
        editor.commit();

        MainActivity mainActivity = (MainActivity) getActivity();
        shopId = mainActivity.shopId;
        shopName = mainActivity.shopName;

        View root = binding.getRoot();
        this.inflater = inflater;
        db = FirebaseFirestore.getInstance();
        ll = root.findViewById(R.id.ll);
        title = root.findViewById(R.id.title);
        textView = root.findViewById(R.id.datePicker);
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

        final Calendar cal = Calendar.getInstance();
        int mYear = cal.get(Calendar.YEAR);
        int mMonth = cal.get(Calendar.MONTH);
        int mDay = cal.get(Calendar.DAY_OF_MONTH);
        getTransaction(mDay,mMonth,mYear);
        fab = root.findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    (view, year, monthOfYear, dayOfMonth) -> getTransaction(dayOfMonth,monthOfYear,year), mYear, mMonth, mDay);
            datePickerDialog.show();
        });
        nsv = root.findViewById(R.id.nested);
        nsv.setOnScrollChangeListener((View.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

            int distance = scrollY-oldScrollY;
            if(distance > 0){
                if(fab.getVisibility() == View.VISIBLE){
                    fab.shrink();
                    Handler handler = new Handler();
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            fab.hide();
                        }
                    };
                    handler.postDelayed(runnable, 150);
                }
            }else{
                if(fab.getVisibility() == View.GONE){
                    fab.show();
                    fab.extend();
                }
            }
        });
        SwipeRefreshLayout swipeRefreshLayout = root.findViewById(R.id.refreshLayout);

        // Refresh  the layout
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {

                        final Calendar cal = Calendar.getInstance();
                        int mYear = cal.get(Calendar.YEAR);
                        int mMonth = cal.get(Calendar.MONTH);
                        int mDay = cal.get(Calendar.DAY_OF_MONTH);
                        getTransaction(mDay,mMonth,mYear);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
        );
        return root;
    }

    void getTransaction(int day, int month, int year){
        totalBought = 0;
        totalSell = 0;

        totalPaid = 0;
        totalUnpaid = 0;
        totalProfit = 0;
        textView.setText(map.get(month + 1) + "-" + day + "-" + year);

        ll.removeAllViews();
        db.collection("Transactions").whereEqualTo("shopId",shopId).orderBy("date").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    int counter = 0;
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot d : list) {


                        View view = inflater.inflate(R.layout.table_view, null, false);

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
                        Transaction c = d.toObject(Transaction.class);
                        String item = c.getItem();
                        String description = c.getDescription();
                        int quantity = c.getQuantity();
                        int bought = c.getBought();
                        int sold = c.getSold();
                        int total = c.getTotal();
                        String soldBy = c.getSoldBy();
                        int paid = c.getPaid();
                        int unpaid = c.getUnpaid();
                        Timestamp date = c.getDate();
                        int Tmonth = date.toDate().getMonth();
                        String[] split = date.toDate().toString().split(" ");
                        int Tyear = Integer.parseInt(split[split.length-1]);
                        if(month == Tmonth && year == Tyear && day == date.toDate().getDate()){
                            itemTV.setText(item);
                            descTV.setText(description);
                            qtyTV.setText(quantity+"");
                            boughtTV.setText(bought+" ETB");
                            soldTV.setText(sold+" ETB");
                            totalTV.setText(total+" ETB");
                            soldByTV.setText(soldBy);
                            dateTV.setText(date.toDate().toString());
                            paidTV.setText(paid+" ETB");
                            unpaidTV.setText(unpaid+" ETB");
                            profitTV.setText(((sold*quantity)-(bought*quantity)) + " ETB");
                            if(counter%2 == 0){
                                view.setBackgroundColor(Color.parseColor("#FFDADADA"));
                            }else{
                                view.setBackgroundColor(Color.parseColor("#ffffff"));
                            }
                            ll.addView(view);
                            totalSell = (sold*quantity)+totalSell;
                            totalBought = (bought*quantity)+totalBought;
                            totalPaid += paid;
                            totalUnpaid += unpaid;
                            totalProfit += ((sold*quantity)-(bought*quantity));
                        }
                    }
                    TextView totalSoldTV = root.findViewById(R.id.totalSold);
                    TextView totalBoughtTV = root.findViewById(R.id.totalBought);
                    TextView totalPaidTV = root.findViewById(R.id.totalPaid);
                    TextView totalUnpaidTV = root.findViewById(R.id.totalUnpaid);
                    TextView totalProfitTV = root.findViewById(R.id.totalProfit);
                    totalSoldTV.setText(totalSell+" ETB");
                    totalBoughtTV.setText(totalBought+" ETB");
                    totalPaidTV.setText(totalPaid+" ETB");
                    totalUnpaidTV.setText(totalUnpaid+" ETB");
                    totalProfitTV.setText(totalProfit+" ETB");
                    totalBought = 0;
                    totalSell = 0;
                } else {
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
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override
    public void onResume() {
        super.onResume();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener((v, keyCode, event) -> {
            if( keyCode == KeyEvent.KEYCODE_BACK )
            {
                return true;
            }
            return false;
        });
    }
}