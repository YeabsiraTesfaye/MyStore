package com.example.myinventory.ui.home;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.myinventory.MainActivity;
import com.example.myinventory.R;
import com.example.myinventory.databinding.FragmentHomeBinding;
import com.example.myinventory.ui.credits.Credit;
import com.example.myinventory.ui.notifications.Transaction;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import kotlin.jvm.internal.Intrinsics;

public class ItemsFragment extends Fragment {

    private FragmentHomeBinding binding;
    private FirebaseFirestore db;
    ProgressBar loadingPB;
    ConstraintLayout cl;
    LinearLayout ll;
    NestedScrollView sv;
    Boolean isFront =true;
    ExtendedFloatingActionButton fab;
    View front;
    View back;
    TextInputLayout nameTIL;
    TextInputLayout descriptionTIL;
    TextInputLayout quantityTIL;
    TextInputLayout boughtTIL;
    TextInputLayout sellTIL;
    ImageView imageViewEdit, delete;
    Button addb;
    ImageButton btnSelect;
    TextView info;
    FirebaseStorage storage;
    StorageReference storageReference;
    private final int PICK_IMAGE_REQUEST = 22;
    Uri filePath;
    FirebaseAuth firebaseAuth;
    ArrayAdapter<String > adapter;
    List<String> items = new ArrayList<>();
    public Boolean action = false;
    private static final int pic_id = 123;
    private static ItemsFragment instance;
    final String dir =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+ "/TheInventory/";
    File newdir = new File(dir);
    SharedPreferences preferences;
    String shopId;
    private String shopName;
    MainActivity mainActivity = new MainActivity();


    public static ItemsFragment newInstance(int index) {
        ItemsFragment fragment = new ItemsFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        preferences = getActivity().getSharedPreferences("pref",MODE_PRIVATE);
        MainActivity mainActivity = (MainActivity) getActivity();
        shopId = mainActivity.shopId;
        shopName = mainActivity.shopName;
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        newdir.mkdirs();
        instance= this;
        firebaseAuth = FirebaseAuth.getInstance();
        info = root.findViewById(R.id.info);
        ll = root.findViewById(R.id.ll);
        sv = root.findViewById(R.id.nested);
        fab = root.findViewById(R.id.fab);
        sv.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                int distance = scrollY-oldScrollY;
                if(distance > 0){
                    if(fab.getVisibility() == View.VISIBLE){
                        fab.shrink();
                        Handler handler = new Handler();
                        Runnable runnable = () -> fab.hide();
                        handler.postDelayed(runnable, 150);
                    }
                }else{
                    if(fab.getVisibility() == View.GONE){
                        fab.show();
                        fab.extend();
                    }
                }
            }
        });
        loadingPB = root.findViewById(R.id.idProgressBar);
        cl = root.findViewById(R.id.view);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        getItems();

        animator(root,inflater,container);
        nameTIL = back.findViewById(R.id.name);
        descriptionTIL = back.findViewById(R.id.description);
        quantityTIL = back.findViewById(R.id.count);
        boughtTIL = back.findViewById(R.id.buy);
        sellTIL = back.findViewById(R.id.sell);
        imageViewEdit = back.findViewById(R.id.imgView);
        addb = back.findViewById(R.id.add);
        delete = back.findViewById(R.id.delete);
        btnSelect = back.findViewById(R.id.addImage);
        popupMenu(btnSelect,"");
        return root;
    }
    public static ItemsFragment GetInstance()
    {
        return instance;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    void animator(View root, LayoutInflater inflater, ViewGroup container){
        front = root.findViewById(R.id.nested);
        back = root.findViewById(R.id.nested2);
        back.setVisibility(View.INVISIBLE);
        fab.setOnClickListener(view -> {
            addb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    String nameInput = nameTIL.getEditText().getText().toString();
                    String descriptionInput = descriptionTIL.getEditText().getText().toString();
                    String quantityInput = quantityTIL.getEditText().getText().toString();
                    String buyInput = boughtTIL.getEditText().getText().toString();
                    String sellInput = sellTIL.getEditText().getText().toString();
                    // validating the text fields if empty or not.
                    if (TextUtils.isEmpty(nameInput)) {
                        nameTIL.setError("Please enter Item Name");
                    } else if (TextUtils.isEmpty(descriptionInput)) {
                        descriptionTIL.setError("Please enter Item Description");
                    } else if (TextUtils.isEmpty(quantityInput)) {
                        quantityTIL.setError("Please enter Item Duration");
                    } else if (TextUtils.isEmpty(buyInput)) {
                        boughtTIL.setError("Please enter Item Duration");
                    } else if (TextUtils.isEmpty(sellInput)) {
                        sellTIL.setError("Please enter Item Duration");
                    } else {
                        addDataToFirestore("Items",nameInput, descriptionInput, Integer.parseInt(quantityInput), Integer.parseInt(buyInput), Integer.parseInt(sellInput)                        );
                    }

                }
            });
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
                    getItems();
                    action = false;
                }

                fab.setIconResource(R.drawable.baseline_add_circle_24);
                fab.setText("ADD ITEM");
                delete.setVisibility(View.GONE);
                addb.setText("ADD");
                info.setText("Add PHOTO");
                delete.setVisibility(View.GONE);
                Handler handler = new Handler();
                Runnable runnable = () -> {
                    back.setVisibility(View.GONE);
                    front.setVisibility(View.VISIBLE);
                    nameTIL.getEditText().setText("");
                    descriptionTIL.getEditText().setText("");
                    quantityTIL.getEditText().setText("");
                    boughtTIL.getEditText().setText("");
                    sellTIL.getEditText().setText("");
                    Glide.with(getContext()).load("").into(imageViewEdit);
                };
                handler.postDelayed(runnable, 200);
            }
        });
    }
    void getItems(){
        if(isConnected()){
            ll.removeAllViews();
            db.collection("Items").whereEqualTo("shopId",shopId).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    String uniqueID = UUID.randomUUID().toString();
                    int size = task.getResult().size();
                    if(size > 0){
                        Query first = db.collection("Items")
                                .whereEqualTo("shopId",shopId)
                                .orderBy("name");

                        first.get()
                                .addOnSuccessListener(documentSnapshots -> {
                                    List<DocumentSnapshot> list = documentSnapshots.getDocuments();
                                    for (DocumentSnapshot d : list) {
                                        Items item = d.toObject(Items.class);
                                        String itemId = d.getId();
                                        if(!items.contains(item.getName())){
                                            items.add(item.getName());
                                        }
                                        View layout = getLayoutInflater().inflate(R.layout.image_view, null, false);
                                        TextView itemNameTV = layout.findViewById(R.id.name);
                                        TextView descriptionTV = layout.findViewById(R.id.description);
                                        TextView priceTV = layout.findViewById(R.id.price);
                                        TextView quantityTV = layout.findViewById(R.id.quantity);
                                        ImageView imageIV = layout.findViewById(R.id.idIVItem);
                                        Button sellBtn = layout.findViewById(R.id.sellBtn);

                                        int originalPrice = item.getSell();

                                        String itemName = item.getName();
                                        String itemDescription = item.getDescription();
                                        int itemQuantity = item.getQuantity();
                                        int itemBought = item.getBuy();
                                        AtomicInteger itemSell = new AtomicInteger(item.getSell());
                                        String imageUri = item.getUri();
                                        layout.setTag(d.getId()+"");

                                        itemNameTV.setText(itemName);
                                        descriptionTV.setText(itemDescription);
                                        priceTV.setText(itemSell+" ETB");
                                        quantityTV.setText(itemQuantity+" In stock");
                                        
                                        if(item.getQuantity() > 0) {
                                            sellBtn.setVisibility(View.VISIBLE);
                                        }else{
                                            sellBtn.setVisibility(View.GONE);
                                        }
                                        sellBtn.setOnClickListener(v -> {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                            builder.setTitle(itemName);
                                            final View sellItemView = getLayoutInflater().inflate(R.layout.sell_item, null);
                                            builder.setView(sellItemView);

                                            NumberPicker np = sellItemView.findViewById(R.id.picker1);
                                            RadioGroup radioGroup = sellItemView.findViewById(R.id.paymentType);
                                            LinearLayout ifCredit = sellItemView.findViewById(R.id.ifCredit);
                                            LinearLayout ifCustome = sellItemView.findViewById(R.id.ifCustomPrice);
                                            CheckBox customCheckBox = sellItemView.findViewById(R.id.customPriceCheckBox);
                                            TextInputLayout customePrice = sellItemView.findViewById(R.id.customPrice);
                                            TextInputLayout customerName = sellItemView.findViewById(R.id.name);
                                            TextInputLayout phone = sellItemView.findViewById(R.id.phone);
                                            TextInputLayout partialPayment = sellItemView.findViewById(R.id.partial);

                                            TextView dp = sellItemView.findViewById(R.id.datePicker);
                                            final Timestamp[] DueDate = new Timestamp[1];
                                            dp.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    final Calendar c1 = Calendar.getInstance();
                                                    int year = c1.get(Calendar.YEAR);
                                                    int month = c1.get(Calendar.MONTH);
                                                    int day = c1.get(Calendar.DAY_OF_MONTH);
                                                    DatePickerDialog datePickerDialog = new DatePickerDialog(
                                                            getContext(),
                                                            new DatePickerDialog.OnDateSetListener() {
                                                                @Override
                                                                public void onDateSet(DatePicker view, int year,
                                                                                      int monthOfYear, int dayOfMonth) {
                                                                    dp.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                                                                    Calendar cal = Calendar.getInstance();
                                                                    cal.set(Calendar.YEAR, year);
                                                                    cal.set(Calendar.MONTH, monthOfYear);
                                                                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                                                    cal.set(Calendar.HOUR_OF_DAY, 0);
                                                                    cal.set(Calendar.MINUTE, 0);
                                                                    cal.set(Calendar.SECOND, 0);
                                                                    cal.set(Calendar.MILLISECOND, 0);
                                                                    DueDate[0] = new Timestamp(cal.getTime());
                                                                }
                                                            },
                                                            year, month, day);
                                                    datePickerDialog.show();
                                                }
                                            });
                                            customCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                @Override
                                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                    if(isChecked){
                                                        ifCustome.setVisibility(View.VISIBLE);
                                                    }else{
                                                        ifCustome.setVisibility(View.GONE);
                                                    }
                                                }
                                            });
                                            radioGroup.setOnCheckedChangeListener(
                                                    (group, checkedId) -> {
                                                        if(checkedId == R.id.cash){
                                                            ifCredit.setVisibility(View.GONE);
                                                        }else if(checkedId == R.id.credit){
                                                            ifCredit.setVisibility(View.VISIBLE);
                                                        }
                                                    });
                                            np.setMaxValue(itemQuantity);
                                            final int[] amount = {0};
                                            np.setOnValueChangedListener((picker, oldVal, newVal) -> amount[0] = picker.getValue());
                                            builder.setPositiveButton("OK", (dialog, which) -> {
                                                String PersonName = customerName.getEditText().getText().toString();
                                                String PhoneNo = phone.getEditText().getText().toString();
                                                int soldQuantity = amount[0];
                                                int PartialPayment = Integer.parseInt(partialPayment.getEditText().getText().toString());
                                                int newQuantity = (itemQuantity-soldQuantity);
                                                Timestamp now = Timestamp.now();

                                                if(amount[0] != 0){
                                                    if(customCheckBox.isChecked()){
                                                        if(customePrice.getEditText().getText().toString().trim().equals("")){
                                                            Toast.makeText(getContext(), "Enter The price!", Toast.LENGTH_SHORT).show();
                                                            return;
                                                        }else {
                                                            itemSell.set(Integer.parseInt(customePrice.getEditText().getText().toString()));
                                                        }
                                                    }
                                                    int selectedId = radioGroup.getCheckedRadioButtonId();
                                                    if (selectedId == R.id.cash) {
                                                        addTransaction(itemName,itemDescription,soldQuantity,itemBought, itemSell.get(), itemSell.get() *soldQuantity,now, itemSell.get() *soldQuantity,0,uniqueID,itemId);
                                                        update(itemName, itemDescription,newQuantity,itemBought, originalPrice, itemId);
                                                        quantityTV.setText(newQuantity+" In Stock");
                                                        if(newQuantity == 0){
                                                            sellBtn.setVisibility(View.GONE);
                                                        }
                                                    }
                                                    else if(selectedId == R.id.credit) {
                                                        if (TextUtils.isEmpty(PersonName) || TextUtils.isEmpty(PhoneNo)|| TextUtils.isEmpty(DueDate[0].toString())) {
                                                            Toast.makeText(getContext(), "Fill out all the necessary fields", Toast.LENGTH_SHORT).show();
                                                        } else if (PartialPayment > itemSell.get() *soldQuantity) {
                                                            Toast.makeText(getContext(), "Partial Payment cant be more than the price", Toast.LENGTH_SHORT).show();
                                                        } else if (PartialPayment == itemSell.get() *soldQuantity) {
                                                            addTransaction(itemName,itemDescription,soldQuantity,itemBought, itemSell.get(), itemSell.get() *soldQuantity,now,PartialPayment,(itemSell.get() *soldQuantity)-PartialPayment,uniqueID,itemId);
                                                            update(itemName, itemDescription,newQuantity,itemBought, originalPrice, itemId);
                                                            quantityTV.setText(newQuantity+" In Stock");
                                                            if(newQuantity == 0){
                                                                sellBtn.setVisibility(View.GONE);
                                                            }
                                                            Toast.makeText(getContext(), "Full payment done", Toast.LENGTH_SHORT).show();
                                                        } else if(PartialPayment < itemSell.get() *soldQuantity) {
                                                            int unpaid = (itemSell.get() * soldQuantity) - PartialPayment;
                                                            Credit credit = new Credit(itemName,PersonName,PhoneNo, itemSell.get(),soldQuantity,PartialPayment,DueDate[0],uniqueID,unpaid,itemId,shopId);
                                                            addCredit(credit);
                                                            update(itemName,itemDescription,newQuantity,itemBought, originalPrice,itemId);
                                                            addTransaction(itemName,itemDescription,soldQuantity,itemBought, itemSell.get(), itemSell.get() *soldQuantity,now,PartialPayment,(itemSell.get() *soldQuantity)-PartialPayment,uniqueID,itemId);

                                                            if(newQuantity == 0){
                                                                sellBtn.setVisibility(View.GONE);
                                                            }
                                                            quantityTV.setText(newQuantity+" In Stock");
                                                            Toast.makeText(getContext(), "Credit saved successfully", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                }else{
                                                    Toast.makeText(getContext(), "Quantity can't be 0!", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            builder.setNegativeButton("CANCEL",((dialog, which) -> {
                                            }));
                                            AlertDialog dialog = builder.create();
                                            dialog.show();
                                        });
                                        if(imageUri != ""){
                                            Glide.with(getContext()).load(imageUri).into(imageIV);
                                        }else{
                                            Glide.with(getContext()).load(R.drawable.image).into(imageIV);
                                        }
                                        layout.setOnClickListener(v -> {
                                            DocumentReference docRef = db.collection("Items").document(itemId);
                                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task1) {
                                                    if (task1.isSuccessful()) {

                                                        DocumentSnapshot document = task1.getResult();
                                                        if (document.exists()) {
                                                            delete.setVisibility(View.VISIBLE);
                                                            fab.performClick();
                                                            nameTIL.getEditText().setText(itemName);
                                                            descriptionTIL.getEditText().setText(itemDescription);
                                                            quantityTIL.getEditText().setText(itemQuantity+"");
                                                            boughtTIL.getEditText().setText(itemBought+"");
                                                            sellTIL.getEditText().setText(itemSell+"");
                                                            addb.setText("UPDATE");
                                                            info.setText("UPDATE PHOTO");
                                                            delete.setVisibility(View.VISIBLE);
                                                            addb.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    String newName = nameTIL.getEditText().getText().toString();
                                                                    String newDescription = descriptionTIL.getEditText().getText().toString();
                                                                    String newQuantity = quantityTIL.getEditText().getText().toString();
                                                                    String newBuy = boughtTIL.getEditText().getText().toString();
                                                                    String newSell = sellTIL.getEditText().getText().toString();
                                                                    // validating the text fields if empty or not.
                                                                    if (TextUtils.isEmpty(newName)) {
                                                                        nameTIL.setError("Please enter Item Name");
                                                                    } else if (TextUtils.isEmpty(newDescription)) {
                                                                        descriptionTIL.setError("Please enter Item Description");
                                                                    } else if (TextUtils.isEmpty(newQuantity)) {
                                                                        quantityTIL.setError("Please enter Item Duration");
                                                                    } else if (TextUtils.isEmpty(newBuy)) {
                                                                        boughtTIL.setError("Please enter Item Duration");
                                                                    } else if (TextUtils.isEmpty(newSell)) {
                                                                        sellTIL.setError("Please enter Item Duration");
                                                                    } else {
                                                                        update(newName, newDescription, Integer.parseInt(newQuantity), Integer.parseInt(newBuy), Integer.parseInt(newSell),itemId);
//                                                                        addDataToFirestore("Items",newName, newDescription, Integer.parseInt(newQuantity), Integer.parseInt(newBuy), Integer.parseInt(newSell)                        );
                                                                    }
                                                                }
                                                            });
                                                            if(imageUri != ""){
                                                                Glide.with(getContext()).load(imageUri).into(imageViewEdit);
                                                            }else{
                                                                Glide.with(getContext()).load(R.drawable.image).into(imageViewEdit);
                                                            }
                                                            fab.show();
                                                            Handler handler = new Handler();
                                                            Runnable runnable = new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    fab.extend();
                                                                    fab.setIconResource(R.drawable.baseline_arrow_back_24);
                                                                    fab.setText("BACK  ");
                                                                }
                                                            };
                                                            handler.postDelayed(runnable, 500);
                                                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                                            delete.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                                    builder.setMessage("Do you really want to Delete "+itemName+"?");
                                                                    builder.setTitle("Alert !");
                                                                    builder.setCancelable(false);
                                                                    builder.setPositiveButton("Yes", (dialog, which) -> {
                                                                        deleteItem(itemId);
                                                                        ll.removeView(layout);
                                                                        fab.performClick();
                                                                    });
                                                                    builder.setNegativeButton("No", (dialog, which) -> {
                                                                        dialog.cancel();
                                                                    });
                                                                    AlertDialog alertDialog = builder.create();
                                                                    alertDialog.show();
                                                                }
                                                            });

                                                        } else {
                                                            Toast.makeText(getContext(), "No such document", Toast.LENGTH_SHORT).show();
                                                            Log.d(TAG, "No such document");
                                                        }
                                                    } else {
                                                        Log.d(TAG, "get failed with ", task1.getException());
                                                    }
                                                }
                                            });
                                        });
                                        popupMenu(layout,d.getId());
                                        ll.addView(layout);
                                    }
                                    loadingPB.setVisibility(View.GONE);
                                    cl.setVisibility(View.GONE);

                                });
                        // [END query_pagination]
                    }else{
                        loadingPB.setVisibility(View.GONE);
                        cl.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "No Item has been found!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }else{
            Toast.makeText(getContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
        }
    }
    private void addDataToFirestore(String collection, String Name, String Description, int Count, int Bought, int Sell) {
        if(isConnected()){
            loadingPB.setVisibility(View.VISIBLE);
            cl.setVisibility(View.VISIBLE);
            String dataID = UUID.randomUUID().toString();
            CollectionReference dbItems = db.collection(collection);
            StorageReference ref = storageReference.child(collection+"/" + dataID);

            if(filePath != null){
                UploadTask uploadTask = ref.putFile(filePath);
                final String[] uri = {""};
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                        return ref.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult(); //this is the download url that you need to pass to your database
                            uri[0] = downloadUri.toString();

                            Items Items = new Items(Name, Description, Count, Bought, Sell, uri[0],shopId);
                            dbItems.add(Items).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    nameTIL.getEditText().setText("");
                                    descriptionTIL.getEditText().setText("");
                                    quantityTIL.getEditText().setText("");
                                    sellTIL.getEditText().setText("");
                                    boughtTIL.getEditText().setText("");
                                    imageViewEdit.setImageDrawable(null);
                                    filePath = null;
                                    Toast.makeText(getContext(), "Your Item has been added.", Toast.LENGTH_SHORT).show();
                                    loadingPB.setVisibility(View.GONE);
                                    cl.setVisibility(View.GONE);
                                    action = true;
                                    isFront = false;
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(), "Fail to add Item \n" + e, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(getContext(), "error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            else {
                Items Items = new Items(Name, Description, Count, Bought, Sell, "",shopId);
                dbItems.add(Items).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        nameTIL.getEditText().setText("");
                        descriptionTIL.getEditText().setText("");
                        quantityTIL.getEditText().setText("");
                        sellTIL.getEditText().setText("");
                        boughtTIL.getEditText().setText("");
                        Toast.makeText(getContext(), "Your Item has been added.", Toast.LENGTH_SHORT).show();
                        loadingPB.setVisibility(View.GONE);
                        cl.setVisibility(View.GONE);
                        action = true;
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Fail to add Item \n" + e, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }else{
            Toast.makeText(getContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
        }

    }
    private void update(String Name, String Description, int Count, int Bought, int Sell, String id) {
        if(isConnected()){
            loadingPB.setVisibility(View.VISIBLE);
            cl.setVisibility(View.VISIBLE);
            String dataID = UUID.randomUUID().toString();
            DocumentReference dbItems = db.collection("Items").document(id);
            StorageReference ref = storageReference.child("Items/" + dataID);
            if(filePath != null){
                UploadTask uploadTask = ref.putFile(filePath);
                final String[] uri = {""};
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (task.isSuccessful()) {
                        }
                        return ref.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult(); //this is the download url that you need to pass to your database
                            uri[0] = downloadUri.toString();
                            Items Items = new Items(Name, Description, Count, Bought, Sell, uri[0],shopId);
                            dbItems.set(Items).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    nameTIL.getEditText().setText("");
                                    descriptionTIL.getEditText().setText("");
                                    quantityTIL.getEditText().setText("");
                                    sellTIL.getEditText().setText("");
                                    boughtTIL.getEditText().setText("");
                                    imageViewEdit.setImageDrawable(null);
                                    filePath = null;
//                                addItemFragment.pb.setVisibility(View.INVISIBLE);
                                    Toast.makeText(getContext(), "Updated Successfully", Toast.LENGTH_SHORT).show();
                                    loadingPB.setVisibility(View.GONE);
                                    cl.setVisibility(View.GONE);
                                    action = true;
                                    isFront = false;
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(), "Fail to add Item \n" + e, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(getContext(), "error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            else {
                dbItems.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Items item = documentSnapshot.toObject(Items.class);
                        Items Items = new Items(Name, Description, Count, Bought, Sell, item.getUri(),shopId);
                        dbItems.set(Items).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(getContext(), "Update Successful", Toast.LENGTH_SHORT).show();
                                action = true;
                                loadingPB.setVisibility(View.GONE);
                                cl.setVisibility(View.GONE);
                                isFront = false;
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), "Fail to add Item \n" + e, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        }else{
            Toast.makeText(getContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
        }

    }
    private void deleteItem(String id) {
        if(isConnected()){
            // below line is for getting the collection
            // where we are storing our Items.
            this.db.collection("Items").
                    document(id).
                    delete().
                    addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Item has been deleted.", Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(getContext(), "Fail to delete the Item. ", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }else{
            Toast.makeText(getContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode,
                resultCode,
                data);
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            // Get the Uri of data
            filePath = data.getData();
            try {

                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(
                                getActivity().getContentResolver(),
                                filePath);

                saveImageToInternalStorage(getContext(),bitmap);
                imageViewEdit.setImageBitmap(bitmap);
            }

            catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }else if (requestCode == pic_id) {
            // BitMap is data structure of image file which store the image in memory
            if (requestCode == pic_id && resultCode == RESULT_OK) {
                Log.d("Demo Pic", "Picture is saved");

                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore
                            .Images
                            .Media
                            .getBitmap(
                                    getActivity().getContentResolver(),
                                    filePath);
                    saveImageToInternalStorage(getContext(),bitmap);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                imageViewEdit.setImageBitmap(bitmap);
            }
        }
    }

    private void SelectImage()
    {

        // Defining Implicit Intent to mobile gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select Image from here..."),
                PICK_IMAGE_REQUEST);
    }

    private void addTransaction(String Name, String Description, int Quantity, int Bought, int Sold, int Total, Timestamp Date, int paid, int unpaid, String id, String itemId) {
        if(isConnected()){
            CollectionReference dbItems = db.collection("Transactions");
            firebaseAuth = FirebaseAuth.getInstance();

            // Initialize firebase user
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();


            Transaction Transaction = new Transaction(Name, Description, firebaseUser.getEmail(), Date, Bought, Sold,Quantity, Total, paid, unpaid, id, itemId, shopId);
            dbItems.add(Transaction).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), "Fail to add Item \n" + e, Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            Toast.makeText(getContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
        }

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


    public void getItemByName(String word){
        if(isConnected()){
            if(word.trim() != "") {
                ll.removeAllViews();
                for (String element : items){
                    if (element.toLowerCase().contains(word.toLowerCase())){
                        Query first = db.collection("Items")
                                .whereEqualTo("name",element)
                                .whereEqualTo("shopId",shopId)
                                .orderBy("name");

                        first.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot documentSnapshots) {

                                List<DocumentSnapshot> list = documentSnapshots.getDocuments();
                                for (DocumentSnapshot d : list) {
                                    Items item = d.toObject(Items.class);
                                    String itemId = d.getId();
                                    if(!items.contains(item.getName())){
                                        items.add(item.getName());
                                    }
                                    View layout = getLayoutInflater().inflate(R.layout.image_view, null, false);

                                    TextView itemNameTV = layout.findViewById(R.id.name);
                                    TextView descriptionTV = layout.findViewById(R.id.description);
                                    TextView priceTV = layout.findViewById(R.id.price);
                                    TextView quantityTV = layout.findViewById(R.id.quantity);
                                    ImageView imageIV = layout.findViewById(R.id.idIVItem);
                                    Button sellBtn = layout.findViewById(R.id.sellBtn);

                                    int originalprice = item.getSell();

                                    String itemName = item.getName();
                                    String itemDescription = item.getDescription();
                                    int itemQuantity = item.getQuantity();
                                    int itemBought = item.getBuy();
                                    AtomicInteger itemSell = new AtomicInteger(item.getSell());
                                    String imageUri = item.getUri();
                                    layout.setTag(d.getId()+"");

                                    itemNameTV.setText(itemName);
                                    descriptionTV.setText(itemDescription);
                                    priceTV.setText(itemSell+" ETB");
                                    quantityTV.setText(itemQuantity+" In stock");



                                    if(item.getQuantity() > 0) {
                                        sellBtn.setVisibility(View.VISIBLE);
                                    }else{
                                        sellBtn.setVisibility(View.GONE);
                                    }
                                    sellBtn.setOnClickListener(v -> {

                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                        builder.setTitle(itemName);
                                        final View sellItemView = getLayoutInflater().inflate(R.layout.sell_item, null);
                                        builder.setView(sellItemView);

                                        NumberPicker np = sellItemView.findViewById(R.id.picker1);
                                        RadioGroup radioGroup = sellItemView.findViewById(R.id.paymentType);
                                        LinearLayout ifCredit = sellItemView.findViewById(R.id.ifCredit);
                                        TextInputLayout customerName = sellItemView.findViewById(R.id.name);
                                        TextInputLayout phone = sellItemView.findViewById(R.id.phone);
                                        TextInputLayout partialPayment = sellItemView.findViewById(R.id.partial);
                                        LinearLayout ifCustome = sellItemView.findViewById(R.id.ifCustomPrice);
                                        CheckBox customCheckBox = sellItemView.findViewById(R.id.customPriceCheckBox);
                                        TextInputLayout customePrice = sellItemView.findViewById(R.id.customPrice);

                                        TextView dp = sellItemView.findViewById(R.id.datePicker);
                                        final Timestamp[] DueDate = new Timestamp[1];
                                        dp.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                final Calendar c1 = Calendar.getInstance();
                                                int year = c1.get(Calendar.YEAR);
                                                int month = c1.get(Calendar.MONTH);
                                                int day = c1.get(Calendar.DAY_OF_MONTH);
                                                DatePickerDialog datePickerDialog = new DatePickerDialog(
                                                        getContext(),
                                                        new DatePickerDialog.OnDateSetListener() {
                                                            @Override
                                                            public void onDateSet(DatePicker view, int year,
                                                                                  int monthOfYear, int dayOfMonth) {
                                                                dp.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                                                                Calendar cal = Calendar.getInstance();
                                                                cal.set(Calendar.YEAR, year);
                                                                cal.set(Calendar.MONTH, monthOfYear);
                                                                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                                                cal.set(Calendar.HOUR_OF_DAY, 0);
                                                                cal.set(Calendar.MINUTE, 0);
                                                                cal.set(Calendar.SECOND, 0);
                                                                cal.set(Calendar.MILLISECOND, 0);
                                                                DueDate[0] = new Timestamp(cal.getTime());
                                                            }
                                                        },
                                                        year, month, day);
                                                datePickerDialog.show();
                                            }
                                        });
                                        if(item.getQuantity() > 0) {
                                            sellBtn.setVisibility(View.VISIBLE);
                                        }else{
                                            sellBtn.setVisibility(View.GONE);
                                        }
                                        radioGroup.setOnCheckedChangeListener(
                                                (group, checkedId) -> {
                                                    if(checkedId == R.id.cash){
                                                        ifCredit.setVisibility(View.GONE);
                                                    }else if(checkedId == R.id.credit){
                                                        ifCredit.setVisibility(View.VISIBLE);
                                                    }
                                                });
                                        np.setMaxValue(itemQuantity);
                                        final int[] amount = {0};
                                        np.setOnValueChangedListener((picker, oldVal, newVal) -> amount[0] = picker.getValue());
                                        builder.setPositiveButton("OK", (dialog, which) -> {
                                            String PersonName = customerName.getEditText().getText().toString();
                                            String PhoneNo = phone.getEditText().getText().toString();
                                            int soldQuantity = amount[0];
                                            int PartialPayment = Integer.parseInt(partialPayment.getEditText().getText().toString());
                                            int newQuantity = (itemQuantity-soldQuantity);
                                            Timestamp now = Timestamp.now();

                                            if(amount[0] != 0){
                                                if(customCheckBox.isChecked()){
                                                    if(customePrice.getEditText().getText().toString().trim().equals("")){
                                                        Toast.makeText(getContext(), "Enter The price!", Toast.LENGTH_SHORT).show();
                                                        return;
                                                    }else {
                                                        itemSell.set(Integer.parseInt(customePrice.getEditText().getText().toString()));
                                                    }
                                                }
                                                String uniqueID = UUID.randomUUID().toString();

                                                int selectedId = radioGroup.getCheckedRadioButtonId();
                                                if (selectedId == R.id.cash) {
                                                    addTransaction(itemName,itemDescription,soldQuantity,itemBought, itemSell.get(), itemSell.get() *soldQuantity,now, itemSell.get() *soldQuantity,0,uniqueID,itemId);
                                                    update(itemName, itemDescription,newQuantity,itemBought, originalprice, itemId);
                                                    quantityTV.setText(newQuantity+" In Stock");
                                                    if(newQuantity == 0){
                                                        sellBtn.setVisibility(View.GONE);
                                                    }
                                                }
                                                else if(selectedId == R.id.credit) {
                                                    if (TextUtils.isEmpty(PersonName) || TextUtils.isEmpty(PhoneNo)|| TextUtils.isEmpty(DueDate[0].toString())) {
                                                        Toast.makeText(getContext(), "Fill out all the necessary fields", Toast.LENGTH_SHORT).show();
                                                    } else if (PartialPayment > itemSell.get() *soldQuantity) {
                                                        Toast.makeText(getContext(), "Partial Payment cant be more than the price", Toast.LENGTH_SHORT).show();
                                                    } else if (PartialPayment == itemSell.get() *soldQuantity) {
                                                        addTransaction(itemName,itemDescription,soldQuantity,itemBought, itemSell.get(), itemSell.get() *soldQuantity,now,PartialPayment,(itemSell.get() *soldQuantity)-PartialPayment,uniqueID,itemId);
                                                        update(itemName, itemDescription,newQuantity,itemBought, originalprice, itemId);
                                                        quantityTV.setText(newQuantity+" In Stock");
                                                        if(newQuantity == 0){
                                                            sellBtn.setVisibility(View.GONE);
                                                        }
                                                        Toast.makeText(getContext(), "Full payment done", Toast.LENGTH_SHORT).show();
                                                    } else if(PartialPayment < itemSell.get() *soldQuantity) {
                                                        int unpaid = (itemSell.get() * soldQuantity) - PartialPayment;
                                                        Credit credit = new Credit(itemName,PersonName,PhoneNo, itemSell.get(),soldQuantity,PartialPayment,DueDate[0],uniqueID,unpaid,itemId,shopId);
                                                        addCredit(credit);
                                                        update(itemName,itemDescription,newQuantity,itemBought, originalprice,itemId);
                                                        addTransaction(itemName,itemDescription,soldQuantity,itemBought, itemSell.get(), itemSell.get() *soldQuantity,now,PartialPayment,(itemSell.get() *soldQuantity)-PartialPayment,uniqueID,itemId);

                                                        if(newQuantity == 0){
                                                            sellBtn.setVisibility(View.GONE);
                                                        }
                                                        quantityTV.setText(newQuantity+" In Stock");
                                                        Toast.makeText(getContext(), "Credit saved successfully", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }else{
                                                Toast.makeText(getContext(), "Quantity can't be 0!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        builder.setNegativeButton("CANCEL",((dialog, which) -> {
                                        }));
                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                    });
                                    if(imageUri != ""){
                                        Glide.with(getContext()).load(imageUri).into(imageIV);
                                    }else{
                                        Glide.with(getContext()).load(R.drawable.image).into(imageIV);
                                    }
                                    layout.setOnClickListener(v -> {
                                        DocumentReference docRef = db.collection("Items").document(itemId);
                                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot document = task.getResult();
                                                    if (document.exists()) {
                                                        delete.setVisibility(View.VISIBLE);
                                                        fab.performClick();
                                                        nameTIL.getEditText().setText(itemName);
                                                        descriptionTIL.getEditText().setText(itemDescription);
                                                        quantityTIL.getEditText().setText(itemQuantity+"");
                                                        boughtTIL.getEditText().setText(itemBought+"");
                                                        sellTIL.getEditText().setText(itemSell+"");
                                                        addb.setText("UPDATE");
                                                        info.setText("UPDATE PHOTO");
                                                        delete.setVisibility(View.VISIBLE);
                                                        addb.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                String newName = nameTIL.getEditText().getText().toString();
                                                                String newDescription = descriptionTIL.getEditText().getText().toString();
                                                                String newQuantity = quantityTIL.getEditText().getText().toString();
                                                                String newBuy = boughtTIL.getEditText().getText().toString();
                                                                String newSell = sellTIL.getEditText().getText().toString();
                                                                // validating the text fields if empty or not.
                                                                if (TextUtils.isEmpty(newName)) {
                                                                    nameTIL.setError("Please enter Item Name");
                                                                } else if (TextUtils.isEmpty(newDescription)) {
                                                                    descriptionTIL.setError("Please enter Item Description");
                                                                } else if (TextUtils.isEmpty(newQuantity)) {
                                                                    quantityTIL.setError("Please enter Item Duration");
                                                                } else if (TextUtils.isEmpty(newBuy)) {
                                                                    boughtTIL.setError("Please enter Item Duration");
                                                                } else if (TextUtils.isEmpty(newSell)) {
                                                                    sellTIL.setError("Please enter Item Duration");
                                                                } else {
//                                                                addDataToFirestore("Items",newName, newDescription, Integer.parseInt(newQuantity), Integer.parseInt(newBuy), Integer.parseInt(newSell)                        );
                                                                    update(newName, newDescription, Integer.parseInt(newQuantity), Integer.parseInt(newBuy), Integer.parseInt(newSell),itemId);

                                                                }
                                                            }
                                                        });
                                                        if(imageUri != ""){
                                                            Glide.with(getContext()).load(imageUri).into(imageViewEdit);
                                                        }else{
                                                            Glide.with(getContext()).load(R.drawable.image).into(imageViewEdit);
                                                        }
                                                        fab.show();
                                                        Handler handler = new Handler();
                                                        Runnable runnable = new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                fab.extend();
                                                                fab.setIconResource(R.drawable.baseline_arrow_back_24);
                                                                fab.setText("BACK  ");
                                                            }
                                                        };
                                                        handler.postDelayed(runnable, 500);
                                                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                                        delete.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                                builder.setMessage("Do you really want to Delete "+itemName+"?");
                                                                builder.setTitle("Alert !");
                                                                builder.setCancelable(false);
                                                                builder.setPositiveButton("Yes", (dialog, which) -> {
                                                                    deleteItem(itemId);
                                                                    ll.removeView(layout);
                                                                    fab.performClick();
                                                                });
                                                                builder.setNegativeButton("No", (dialog, which) -> {
                                                                    dialog.cancel();
                                                                });
                                                                AlertDialog alertDialog = builder.create();
                                                                alertDialog.show();
                                                            }
                                                        });

                                                    } else {
                                                        Toast.makeText(getContext(), "No such document", Toast.LENGTH_SHORT).show();
                                                        Log.d(TAG, "No such document");
                                                    }
                                                } else {
                                                    Log.d(TAG, "get failed with ", task.getException());
                                                }
                                            }
                                        });
                                    });
                                    popupMenu(layout,d.getId());
                                    ll.addView(layout);
                                }
                                loadingPB.setVisibility(View.GONE);
                                cl.setVisibility(View.GONE);
                                DocumentSnapshot lastVisible = documentSnapshots.getDocuments()
                                        .get(documentSnapshots.size() -1);

                            }
                        });
                    }
                }
            }
            else{
                getItems();
            }
        }else{
            Toast.makeText(getContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
        }

    }
    private void popupMenu(View v, String id) {
        if(v.getId() != R.id.addImage){
            PopupMenu var10000 = new PopupMenu(getContext(),v, Gravity.RIGHT);
            final PopupMenu popupMenu = var10000;
            popupMenu.inflate(R.menu.popup_menu);
            popupMenu.setOnMenuItemClickListener(it -> {
                Intrinsics.checkNotNullExpressionValue(it, "it");
                int var2 = it.getItemId();
                boolean var100001;
                if (var2 == R.id.call) {
                    deleteItem(id);
                    ll.removeView(v);
                    var100001 = true;
                } else {
                    var100001 = true;
                }
                return var100001;
            });
            v.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View it) {
                    try {
                        Field popup = PopupMenu.class.getDeclaredField("mPopup");
                        Intrinsics.checkNotNullExpressionValue(popup, "popup");
                        popup.setAccessible(true);
                        Object menu = popup.get(popupMenu);
                        menu.getClass().getDeclaredMethod("setForceShowIcon", Boolean.TYPE).invoke(menu, true);
                    } catch (Exception var6) {
                        Log.d("error", var6.toString());
                    } finally {
                        popupMenu.show();
                    }
                    return true;
                }
            });
        }else{
            PopupMenu var10000 = new PopupMenu(getContext(),v, Gravity.RIGHT);
            final PopupMenu popupMenu = var10000;
            popupMenu.inflate(R.menu.photo_source_selector);
            popupMenu.setOnMenuItemClickListener(it -> {
                Intrinsics.checkNotNullExpressionValue(it, "it");
                int var2 = it.getItemId();
                boolean var100001 = false;
                if (var2 == R.id.storage) {
                    SelectImage();
                    var100001 = true;
                } else if (var2 == R.id.camera){
                    String file = dir+ DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString()+".jpg";
                    File newfile = new File(file);
                    try {
                        newfile.createNewFile();
                    } catch (IOException e) {}
                    filePath = FileProvider.getUriForFile(getContext(), "com.example.myinventory" + ".provider",newfile);
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, filePath);
                    cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);


                    startActivityForResult(cameraIntent, pic_id);
                    var100001 = true;
                }
                return var100001;
            });
            v.setOnClickListener(view ->{
                try {
                    Field popup = PopupMenu.class.getDeclaredField("mPopup");
                    Intrinsics.checkNotNullExpressionValue(popup, "popup");
                    popup.setAccessible(true);
                    Object menu = popup.get(popupMenu);
                    menu.getClass().getDeclaredMethod("setForceShowIcon", Boolean.TYPE).invoke(menu, true);
                } catch (Exception var6) {
                    Log.d("error", var6.toString());
                } finally {
                    popupMenu.show();
                }
            });
        }
    }
    void addCredit(Credit credit){
        if(isConnected()){
            CollectionReference dbItems = db.collection("Credits");
            firebaseAuth = FirebaseAuth.getInstance();
            dbItems.add(credit).addOnSuccessListener(documentReference -> {

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), "Fail to add Item \n" + e, Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            Toast.makeText(getContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
        }
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





    public Uri saveImageToInternalStorage(Context mContext, Bitmap bitmap){

        ContextWrapper wrapper = new ContextWrapper(mContext);

//        File file = wrapper.getDir("Images",MODE_PRIVATE);

        File file = new File(dir+ DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString()+"new.jpg");

        try{

            OutputStream stream = null;

            stream = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.JPEG,30,stream);
            stream.flush();

            stream.close();

        }catch (IOException e)
        {
            e.printStackTrace();
        }

        Uri mImageUri = Uri.parse(file.getAbsolutePath());
        System.out.println("My uri"+mImageUri);

        this.filePath = FileProvider.getUriForFile(getContext(), "com.example.myinventory" + ".provider",file);

        return mImageUri;
    }
}