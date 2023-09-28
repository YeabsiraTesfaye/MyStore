package com.example.myinventory.ui.orders;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.example.myinventory.R;
import com.example.myinventory.databinding.FragmentDashboardBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.List;

public class OrdersFragment extends Fragment {
    private FragmentDashboardBinding binding;
    private FirebaseFirestore db;
    ProgressBar loadingPB;
    LinearLayout ll;
    NestedScrollView sv;
    Boolean isFront =true;
    View front;
    View back;
    ExtendedFloatingActionButton fab;
    Button save;

    TextInputLayout orderTV;
    TextInputLayout orderedByTV;
    TextInputLayout quantityTV;
    TextInputLayout phoneTV;
    TextView dp;
    Boolean action = false;

    public static OrdersFragment newInstance(int index) {
        OrdersFragment fragment = new OrdersFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        ll = root.findViewById(R.id.ll);
        sv = root.findViewById(R.id.nested1);
        fab = root.findViewById(R.id.fab);
        animator(root,inflater,container);
        save = root.findViewById(R.id.save);

        orderTV = root.findViewById(R.id.order);
        orderedByTV = root.findViewById(R.id.orderedby);
        phoneTV = root.findViewById(R.id.phone);
        quantityTV = root.findViewById(R.id.quantity);
        dp = root.findViewById(R.id.datePicker);
        final String[] dpS = {""};
        dp.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        dp.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year1);
                        dpS[0] = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year1;
                    },
                    year, month, day);
            datePickerDialog.show();
        });
        save.setOnClickListener(v -> {
            String orderS = orderTV.getEditText().getText().toString();
            String orderedByS = orderedByTV.getEditText().getText().toString();
            String phoneS = phoneTV.getEditText().getText().toString();
            String quantityS = quantityTV.getEditText().getText().toString();
            if (TextUtils.isEmpty(orderS)) {
                orderTV.setError("Please enter Item Name");
            } else if (TextUtils.isEmpty(orderedByS)) {
                orderedByTV.setError("Please enter Item Description");
            } else if (TextUtils.isEmpty(quantityS)) {
                quantityTV.setError("Please enter Item Duration");
            } else if (TextUtils.isEmpty(dpS[0])) {
                dp.setError("Please enter Item Duration");
            } else {
                addDataToFirestore(orderS,orderedByS,phoneS,Integer.parseInt(quantityS),dpS[0]);
            }
        });
        sv.setOnScrollChangeListener((View.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            int distance = scrollY-oldScrollY;
            if(distance > 0){
                if(fab.getVisibility() == View.VISIBLE){
                    fab.hide();
                }
            }else{
                if(fab.getVisibility() == View.GONE){
                    fab.show();
                }
            }
        });
        loadingPB = root.findViewById(R.id.idProgressBar);
        db = FirebaseFirestore.getInstance();
        loadingPB = root.findViewById(R.id.idProgressBar);
        getItems(db,inflater,container);
        return root;
    }
    void animator(View root, LayoutInflater inflater, ViewGroup container){
        front = root.findViewById(R.id.nested1);
        back = root.findViewById(R.id.nested);
        back.setVisibility(View.INVISIBLE);
        fab.setOnClickListener(view -> {
            if(isFront)
            {
                back.setVisibility(View.VISIBLE);
                front.setVisibility(View.GONE);
                TranslateAnimation animate = new TranslateAnimation(0, 0, back.getHeight(), 0);
                animate.setDuration(200);
                animate.setFillAfter(true);
                back.startAnimation(animate);
                fab.setIconResource(R.drawable.baseline_arrow_back_24);
                fab.setText("BACK");
                isFront = false;
            }
            else
            {
                TranslateAnimation animate = new TranslateAnimation(0, 0, 0, back.getHeight());
                animate.setDuration(200);
                animate.setFillAfter(true);
                back.startAnimation(animate);
                isFront =true;
                if(action){
                    getItems(db,inflater,container);
                    action = false;
                }
                Handler handler = new Handler();
                Runnable runnable = () -> {
                    back.setVisibility(View.GONE);
                    front.setVisibility(View.VISIBLE);
                };handler.postDelayed(runnable, 200);
            }
        });
    }
    private void addDataToFirestore(String order, String orderedBy, String phone, int quantity, String date) {
        loadingPB.setVisibility(View.VISIBLE);
        CollectionReference dbOrder = db.collection("Orders");
        Order data  = new Order(order, orderedBy, phone, date, quantity);
        dbOrder.add(data).addOnSuccessListener(documentReference -> {
            Toast.makeText(getContext(), "Order saved successfully", Toast.LENGTH_SHORT).show();
            loadingPB.setVisibility(View.GONE);
            action = true;
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Fail to add Item \n" + e, Toast.LENGTH_SHORT).show();
            }
        });
    }
    void getItems(FirebaseFirestore db, LayoutInflater inflater, ViewGroup container){
        ll.removeAllViews();
        db.collection("Orders").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    loadingPB.setVisibility(View.GONE);
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot d : list) {
                        Order c = d.toObject(Order.class);
                        View layout = inflater.inflate(R.layout.list_order, null, false);
                        layout.setTag(d.getId()+"");
                        TextView order = layout.findViewById(R.id.order);
                        TextView orderedBy = layout.findViewById(R.id.orderedBy);
                        TextView phone = layout.findViewById(R.id.phone);
                        TextView date = layout.findViewById(R.id.date);
                        TextView quantity = layout.findViewById(R.id.quantity);
                        Button save = layout.findViewById(R.id.done);

                        if(c.getPhone() == null || c.getPhone().equals("")){
                            phone.setVisibility(View.GONE);
                        }
                        order.setText(c.getOrder());
                        orderedBy.setText(c.getOrderedBy());
                        phone.setText(c.getPhone());
                        date.setText(c.getDate());
                        quantity.setText(c.getQuantity()+"");
                        ll.addView(layout);
                        save.setOnClickListener(v -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle(c.getOrder());
                            builder.setMessage("Are you sure?");
                            builder.setPositiveButton("OK", (dialog, which) -> {
                                deleteItem(d.getId());
                                ll.removeView(layout);
                            });
                            builder.setNegativeButton("CANCEL",((dialog, which) -> {
                            }));
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        });
                    }
                } else {
//                    Toast.makeText(getContext(), "No data found.", Toast.LENGTH_SHORT).show();
                    loadingPB.setVisibility(View.GONE);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Fail to get the data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void deleteItem(String id) {
        this.db.collection("Orders").
        document(id).
        delete().
        addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Order has been done successfully", Toast.LENGTH_SHORT).show();
//                            getItems(db, inflater, container);
                } else {
                    Toast.makeText(getContext(), "Fail to delete the Order. ", Toast.LENGTH_SHORT).show();
                }
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
        isFront = true;
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener((v, keyCode, event) -> {
            if( keyCode == KeyEvent.KEYCODE_BACK )
            {
                if(!isFront){
                    fab.performClick();
                }
                return true;
            }
            return false;
        });
    }
}