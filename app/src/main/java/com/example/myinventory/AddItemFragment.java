//package com.example.myinventory;
//
//import static android.app.Activity.RESULT_OK;
//
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.net.Uri;
//import android.os.Bundle;
//import android.provider.MediaStore;
//import android.text.TextUtils;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.ProgressBar;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.fragment.app.Fragment;
//
//import com.google.android.gms.tasks.Continuation;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.android.gms.tasks.Task;
//import com.google.android.material.textfield.TextInputLayout;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.firestore.CollectionReference;
//import com.google.firebase.firestore.DocumentReference;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.storage.FileDownloadTask;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.StorageReference;
//import com.google.firebase.storage.UploadTask;
//
//import java.io.IOException;
//import java.util.UUID;
//
//public class AddItemFragment extends Fragment {
//
//
//    public  TextInputLayout name, description, count, bought, sell;
//
//    // creating variable for button
//    public  Button add;
//
//    // creating a strings for storing
//    // our values from edittext fields.
//    public  String name1, description1, count1, bought1, sell1;
//
//
//    // creating a variable
//    // for firebasefirestore.
//    public  FirebaseFirestore db;
//
//    public  ImageView imageView;
//    public  Button btnSelect;
//
//    // Uri indicates, where the image will be picked from
//    public Uri filePath;
//
//    public FirebaseStorage storage;
//    public StorageReference storageReference;
//
//    // request code
//    public  final int PICK_IMAGE_REQUEST = 22;
//
//    public FirebaseAuth fAuth;
//    public FirebaseFirestore fStore;
//    String UID;
//    public ProgressBar pb;
//
//    public AddItemFragment() {
//    }
//
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             ViewGroup container, Bundle savedInstanceState) {
//
//
//        View root = inflater.inflate(R.layout.fragment_add_item, container, false);
//
//        pb = root.findViewById(R.id.idProgressBar2);
//        // initialise views
//        btnSelect = root.findViewById(R.id.addImage);
//        imageView = root.findViewById(R.id.imgView);
//
//        // get the Firebase  storage reference
//        storage = FirebaseStorage.getInstance();
//        storageReference = storage.getReference();
//
//        fAuth = FirebaseAuth.getInstance();
//
//        fStore = FirebaseFirestore.getInstance();
//
//        btnSelect.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v)
//            {
//                SelectImage();
//            }
//        });
//
//        db = FirebaseFirestore.getInstance();
//
//        // initializing our edittext and buttons
//
//        name = root.findViewById(R.id.name);
//        description = root.findViewById(R.id.description);
//        count = root.findViewById(R.id.count);
//        sell = root.findViewById(R.id.sell);
//        bought = root.findViewById(R.id.buy);
//
//        add = root.findViewById(R.id.add);
//
//        add.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                pb.setVisibility(View.VISIBLE);
//                pb.bringToFront();
//                // getting data from edittext fields.
//                name1 = name.getEditText().getText().toString();
//                description1 = description.getEditText().getText().toString();
//                count1 = count.getEditText().getText().toString();
//                bought1 = bought.getEditText().getText().toString();
//                sell1 = sell.getEditText().getText().toString();
//
//
//                // validating the text fields if empty or not.
//                if (TextUtils.isEmpty(name1)) {
//                    name.setError("Please enter Course Name");
//                } else if (TextUtils.isEmpty(description1)) {
//                    description.setError("Please enter Course Description");
//                } else if (TextUtils.isEmpty(count1)) {
//                    count.setError("Please enter Course Duration");
//                } else if (TextUtils.isEmpty(bought1)) {
//                    bought.setError("Please enter Course Duration");
//                } else if (TextUtils.isEmpty(sell1)) {
//                    sell .setError("Please enter Course Duration");
//                } else {
//                    // calling method to add data to Firebase Firestore.
//                    addDataToFirestore(name1, description1, Integer.parseInt(count1), Integer.parseInt(bought1), Integer.parseInt(sell1)                        );
//                }
//            }
//        });
//
////        final TextView textView = binding.textGallery;
////        galleryViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
//        return root;
//    }
//
//    private void addDataToFirestore(String Name, String Description, int Count, int Bought, int Sell) {
//
//        String dataID = UUID.randomUUID().toString();
//        CollectionReference dbCourses = db.collection("Items");
//
//        StorageReference ref
//                = storageReference
//                .child(
//                        "Items/"
//                                + dataID);
//        if(filePath != null){
//            UploadTask uploadTask = ref.putFile(filePath);
//            final String[] uri = {""};
//
//            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
//                @Override
//                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
//                    if (task.isSuccessful()) {
//                        //here the upload of the image finish
//                        Toast.makeText(getContext(), "Image Uploaded!!", Toast.LENGTH_SHORT).show();
//                    }
//                    return ref.getDownloadUrl();
//
//                }
//            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
//                @Override
//                public void onComplete(@NonNull Task<Uri> task) {
//                    if (task.isSuccessful()) {
//                        Uri downloadUri = task.getResult(); //this is the download url that you need to pass to your database
//                        uri[0] = downloadUri.toString();
//
//                        Items courses = new Items(Name, Description, Count, Bought, Sell, uri[0]);
//
//                        // below method is use to add data to Firebase Firestore.
//                        dbCourses.add(courses).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                            @Override
//                            public void onSuccess(DocumentReference documentReference) {
//                                name.getEditText().setText("");
//                                description.getEditText().setText("");
//                                count.getEditText().setText("");
//                                sell.getEditText().setText("");
//                                bought.getEditText().setText("");
//                                imageView.setImageDrawable(null);
//                                filePath = null;
//                                pb.setVisibility(View.INVISIBLE);
//                                Toast.makeText(getContext(), "Your Course has been added to Firebase Firestore", Toast.LENGTH_SHORT).show();
//                            }
//                        }).addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Toast.makeText(getContext(), "Fail to add course \n" + e, Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    } else {
//                        Toast.makeText(getContext(), "error", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });
//
//        }
//        else {
//
//            Items courses = new Items(Name, Description, Count, Bought, Sell, "");
//
//            // below method is use to add data to Firebase Firestore.
//            dbCourses.add(courses).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                @Override
//                public void onSuccess(DocumentReference documentReference) {
//                    // after the data addition is successful
//                    // we are displaying a success toast message.
////                uploadImage(dataID);
//                    name.getEditText().setText("");
//                    description.getEditText().setText("");
//                    count.getEditText().setText("");
//                    sell.getEditText().setText("");
//                    bought.getEditText().setText("");
//                    pb.setVisibility(View.INVISIBLE);
//                    Toast.makeText(getContext(), "Your Course has been added to Firebase Firestore", Toast.LENGTH_SHORT).show();
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    // this method is called when the data addition process is failed.
//                    // displaying a toast message when data addition is failed.
//                    Toast.makeText(getContext(), "Fail to add course \n" + e, Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//    }
//
//    private void SelectImage()
//    {
//
//        // Defining Implicit Intent to mobile gallery
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(
//                Intent.createChooser(
//                        intent,
//                        "Select Image from here..."),
//                PICK_IMAGE_REQUEST);
//    }
//
//
//    @Override
//    public void onActivityResult(int requestCode,
//                                 int resultCode,
//                                 Intent data)
//    {
//
//        super.onActivityResult(requestCode,
//                resultCode,
//                data);
//
//        // checking request code and result code
//        // if request code is PICK_IMAGE_REQUEST and
//        // resultCode is RESULT_OK
//        // then set image in the image view
//        if (requestCode == PICK_IMAGE_REQUEST
//                && resultCode == RESULT_OK
//                && data != null
//                && data.getData() != null) {
//
//            // Get the Uri of data
//            filePath = data.getData();
//            try {
//
//                // Setting image on image view using Bitmap
//                Bitmap bitmap = MediaStore
//                        .Images
//                        .Media
//                        .getBitmap(
//                                getActivity().getContentResolver(),
//                                filePath);
//                imageView.setImageBitmap(bitmap);
//            }
//
//            catch (IOException e) {
//                // Log the exception
//                e.printStackTrace();
//            }
//        }
//    }
//
//}