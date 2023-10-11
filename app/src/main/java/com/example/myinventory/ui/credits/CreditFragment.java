package com.example.myinventory.ui.credits;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myinventory.LoginActivity;
import com.example.myinventory.MainActivity;
import com.example.myinventory.ui.home.Items;
import com.example.myinventory.R;
import com.example.myinventory.ui.notifications.Transaction;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CreditFragment extends Fragment {
    MaterialCardView uid;
    int click = 0;
    LinearLayout creditsLL;
    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;

    AtomicInteger paid;
    AtomicInteger unpaid;
    private SharedPreferences preferences;
    private String shopId;
    private String shopName;
    MainActivity mainActivity = new MainActivity();

    public CreditFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        preferences = getActivity().getSharedPreferences("pref",MODE_PRIVATE);
        MainActivity mainActivity = (MainActivity) getActivity();
        shopId = mainActivity.shopId;
        shopName = mainActivity.shopName;
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_credit, container, false);
        creditsLL = view.findViewById(R.id.credits);
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        getCredits();
        return view;
    }

    void getCredits(){
        if(isConnected()){
            db.collection("Credits").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        Query first = queryDocumentSnapshots
                                .getQuery()
                                .whereEqualTo("shopId",shopId)
                                .orderBy("dueDate",Query.Direction.DESCENDING);
                        first.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                int counter = 0;
                                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                                for (DocumentSnapshot d : list) {

                                    View view = getLayoutInflater().inflate(R.layout.credite_view, null, false);
                                    if (counter % 2 == 0) {
                                        view.setBackgroundColor(Color.parseColor("#FFDADADA"));
                                    } else {
                                        view.setBackgroundColor(Color.parseColor("#ffffff"));
                                    }
                                    counter++;
                                    TextView itemTV = view.findViewById(R.id.item);
                                    TextView nameTV = view.findViewById(R.id.name);
                                    TextView phoneTV = view.findViewById(R.id.phone);
                                    TextView priceTV = view.findViewById(R.id.price);
                                    TextView quantityTV = view.findViewById(R.id.quantity);
                                    TextView paidTV = view.findViewById(R.id.partial);
                                    TextView unpaidTV = view.findViewById(R.id.unpaid);
                                    TextView dateTV = view.findViewById(R.id.duedate);
                                    Credit c = d.toObject(Credit.class);
                                    String item = c.getItemName();
                                    String name = c.getPersonName();
                                    String phone = c.getPhone();
                                    int price = c.getItemPrice();
                                    int quantity = c.getItemQuantity();
                                    paid = new AtomicInteger(c.getPartialPayments());
                                    unpaid = new AtomicInteger(c.getUnpaid());
                                    Timestamp duedate = c.getDueDate();


                                    itemTV.setText(item);
                                    nameTV.setText(name);
                                    phoneTV.setText(phone);
                                    priceTV.setText(price + " ETB");
                                    quantityTV.setText(quantity+"");
                                    paidTV.setText(paid + " ETB");
                                    unpaidTV.setText(unpaid + " ETB");
                                    dateTV.setText(duedate.toDate().toString());



                                    view.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                            builder.setTitle("Credit Payment");
                                            final View customLayout = getLayoutInflater().inflate(R.layout.credit_payment_view, null);
                                            builder.setView(customLayout);
                                            EditText np = customLayout.findViewById(R.id.np);
                                            RadioGroup radioGroup = customLayout.findViewById(R.id.type);
                                            LinearLayout ifPartial = customLayout.findViewById(R.id.ifPartial);
                                            radioGroup.setOnCheckedChangeListener(
                                                    (group, checkedId) -> {
                                                        if(checkedId == R.id.fullPayment){
                                                            ifPartial.setVisibility(View.GONE);
                                                        }else if(checkedId == R.id.partialPayment){
                                                            ifPartial.setVisibility(View.VISIBLE);
                                                        }
                                                    });



                                            builder.setPositiveButton("OK", (dialog, which) -> {
                                                int selectedId = radioGroup.getCheckedRadioButtonId();
                                                if (selectedId == R.id.partialPayment) {
                                                    int amount = Integer.parseInt(np.getText().toString());

                                                    if (amount != 0) {

                                                        if (amount < c.getUnpaid()) {
                                                            paid.set(c.getPartialPayments() + amount);
                                                            unpaid.set(c.getUnpaid() - amount);
                                                            c.setPartialPayments(paid.get());
                                                            c.setUnpaid(unpaid.get());
                                                            update(c, d.getId());
                                                            getItemById(c.getItemId(),quantity, c.getItemPrice()*c.getItemQuantity(), c.getId());
                                                            paidTV.setText(paid+" ETB");
                                                            unpaidTV.setText(unpaid+" ETB");
                                                        } else if (amount == c.getUnpaid()) {

                                                            AlertDialog.Builder builder2 = new AlertDialog.Builder(getContext());
                                                            builder2.setTitle("Full Payment Complete!");
                                                            builder2.setMessage("Are you sure?");
                                                            builder2.setPositiveButton("OK", (dialog2, which2) -> {
                                                                paid.set(c.getPartialPayments() + amount);
                                                                unpaid.set(c.getUnpaid() - amount);
                                                                c.setPartialPayments(paid.get());
                                                                c.setUnpaid(unpaid.get());
                                                                creditsLL.removeView(view);
                                                                delete(d.getId());
                                                                getItemById(c.getItemId(),quantity, c.getItemPrice()*c.getItemQuantity(),c.getId());

                                                            });
                                                            builder2.setNegativeButton("CANCEL",((dialog2, which2) -> {

                                                            }));
                                                            AlertDialog dialog2 = builder2.create();
                                                            dialog2.show();


                                                        } else if (amount > c.getUnpaid()) {
                                                            paid.set(c.getPartialPayments() + amount);
                                                            unpaid.set(c.getUnpaid() - amount);
                                                            c.setPartialPayments(paid.get());
                                                            c.setUnpaid(unpaid.get());
                                                            System.out.println(amount+" +++++++++++++ "+c.getUnpaid());
                                                            Toast.makeText(getContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                } else if (selectedId == R.id.fullPayment) {
                                                    AlertDialog.Builder builder2 = new AlertDialog.Builder(getContext());
                                                    builder2.setTitle("Full Payment Complete!");
                                                    builder2.setMessage("Are you sure?");
                                                    builder2.setPositiveButton("OK", (dialog2, which2) -> {
                                                        paid.set(c.getPartialPayments() + c.getUnpaid());
                                                        unpaid.set(0);
                                                        delete(d.getId());
                                                        creditsLL.removeView(view);
                                                        getItemById(c.getItemId(),quantity,c.getItemPrice()*c.getItemQuantity(),c.getId());

                                                    });
                                                    builder2.setNegativeButton("CANCEL",((dialog2, which2) -> {

                                                    }));
                                                    AlertDialog dialog2 = builder2.create();
                                                    dialog2.show();


                                                }
                                            });
                                            builder.setNegativeButton("CANCEL",((dialog, which) -> {
                                            }));



                                            AlertDialog dialog = builder.create();
                                            dialog.show();
                                        }
                                    });
                                    creditsLL.addView(view);
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        });;

                    } else {
//                    Toast.makeText(getContext(), "No data found in Database", Toast.LENGTH_SHORT).show();
                    }
                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), "Fail to get the data.", Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            Toast.makeText(getContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
        }
    }

    private void update(Credit c,String id)
    {
        if(isConnected()){
            DocumentReference dbItems = db.collection("Credits").document(id);
            dbItems.set(c).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {

                    Toast.makeText(getContext(), "Updated Successfully", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), "Fail to update Credit \n" + e, Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            Toast.makeText(getContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
        }
    }
    private void delete(String id) {
        if(isConnected()){
            // below line is for getting the collection
            // where we are storing our Items.
            this.db.collection("Credits").
                    document(id).
                    delete().
                    addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Done", Toast.LENGTH_SHORT).show();
//                            getItems(db, inflater, container);
                            } else {
                                Toast.makeText(getContext(), "Fail to delete the Item. ", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }else{
            Toast.makeText(getContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
        }

    }
    private void getItemById(String id,int qty,int total,String uniqueID) {
        if(isConnected()){
            db.collection("Items").whereEqualTo("shopId",shopId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot d : list) {
                            if(d.getId().equals(id)){
                                Items item = d.toObject(Items.class);
                                Timestamp timestamp = Timestamp.now();
                                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                                Transaction t = new Transaction(item.getName(),item.getDescription(),firebaseUser.getEmail(),timestamp,item.getBuy(),item.getSell(),qty,total,paid.get(), unpaid.get(), uniqueID, id, shopId);

//                            addTransaction(c.getName(),c.getDescription(),qty,c.getBuy(),c.getSell(),total,timestamp);
                                updateTransaction(t, uniqueID);
                            }


                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    System.out.println(e +" exception two");
                }
            });
        }else{
            Toast.makeText(getContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
        }

    }

//    private void addTransaction(String Name, String Description, int Quantity, int Bought, int Sold, int Total, Timestamp Date) {
//        CollectionReference dbItems = db.collection("Transactions");
//        firebaseAuth = FirebaseAuth.getInstance();
//
//        // Initialize firebase user
//        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
//
//
////        Transaction Transaction = new Transaction(Name, Description, Date, Bought, Sold, Quantity, Total,firebaseUser.getEmail());
//        Transaction Transaction = new Transaction(Name,Description,"",Date,Bought,Sold,Sold,Quantity,paid.get(),unpaid.get());
//
//        dbItems.add(Transaction).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//            @Override
//            public void onSuccess(DocumentReference documentReference) {
//                Toast.makeText(getContext(), "Transaction added ", Toast.LENGTH_SHORT).show();
//
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(getContext(), "Fail to add Item \n" + e, Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
    private void updateTransaction(Transaction transaction, String id){
        if(isConnected()){
            Query query = db.collection("Transactions")
                    .whereEqualTo("id",id)
                    .whereEqualTo("shopId",shopId)
                    .limit(1);

            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    System.out.println(id+" <<<<<<<<<<<>>>>>>>>>>");
                    task.addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (DocumentSnapshot d : queryDocumentSnapshots) {
                                Transaction t = d.toObject(Transaction.class);
                                db.collection("Transactions").document(d.getId()).set(transaction);
                                System.out.println(d.getId()+" <<<<<<<<<<<>>>>>>>>>>");
                            }
                        }
                    });
                }
            });
        }else{
            Toast.makeText(getContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
        }


//        db.collection("Transactions").whereEqualTo("id",id).limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//            @Override
//            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//
//                for (QueryDocumentSnapshot qs :
//                        queryDocumentSnapshots) {
//                    System.out.println(qs.getId()+" >>>>");
//                    db.collection("Transactions").document(qs.getId()).set(transaction).addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void unused) {
//
//                        }
//                    });
//
//                }
//            }
//        });
    }
    public boolean isConnected() {
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            return connected;
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
        return connected;
    }
}