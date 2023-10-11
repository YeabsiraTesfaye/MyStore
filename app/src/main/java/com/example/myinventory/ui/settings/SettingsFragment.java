package com.example.myinventory.ui.settings;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.example.myinventory.AddItemsFromExcelActivity;
import com.example.myinventory.AddShopActivity;
import com.example.myinventory.AdvancedActivity;
import com.example.myinventory.LoginActivity;
import com.example.myinventory.MainActivity;
import com.example.myinventory.R;
import com.example.myinventory.ReallocateItemActivity;
import com.example.myinventory.SetupPwdActivity;
import com.example.myinventory.databinding.FragmentNotificationsBinding;
import com.example.myinventory.databinding.FragmentSettingsBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsFragment extends Fragment {
    FragmentSettingsBinding binding;
    CircleImageView civ;
    TextView name, email;
    Button btLogout,changePwd, advanced, shop, reallocate, importExcel;
    ToggleButton toggleButton;
    FirebaseAuth firebaseAuth;
    GoogleSignInClient googleSignInClient;
    SharedPreferences sh;
    ConstraintLayout cl;
    private String shopId;
    private String shopName;
    Uri filePath;
    public static final int REQUEST_CODE = 1;
    private static final String LOG_TAG = "AndroidExample";

    private static final int pic_id = 123;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        sh  = getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
        binding =  FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        MainActivity mainActivity = (MainActivity) getActivity();
        shopId = mainActivity.shopId;
        shopName = mainActivity.shopName;
        toggleButton = root.findViewById(R.id.toggleButton);
        civ = root.findViewById(R.id.circleImageView);
        name = root.findViewById(R.id.name);
        email = root.findViewById(R.id.mail);
        btLogout = root.findViewById(R.id.logoutBtn);
        changePwd = root.findViewById(R.id.change);
        advanced = root.findViewById(R.id.transaction);
        shop = root.findViewById(R.id.shop);
        reallocate = root.findViewById(R.id.allocate);
        importExcel = root.findViewById(R.id.itemsFromExcel);
        cl = root.findViewById(R.id.fp);
        cl.setOnClickListener(view ->{
            toggleButton.performClick();
        });


        Boolean fp = sh.getBoolean("fp", false);

        toggleButton.setChecked(fp);
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor myEdit = sh.edit();


                myEdit.putBoolean("fp", toggleButton.isChecked());
                myEdit.commit();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize firebase user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        // Check condition
        if (firebaseUser != null) {
            // When firebase user is not equal to null set image on image view
            Glide.with(getContext()).load(firebaseUser.getPhotoUrl()).into(civ);
            // set name on text view
            name.setText(firebaseUser.getDisplayName());
            email.setText(firebaseUser.getEmail());
        }

        changePwd.setOnClickListener(view ->{
            Intent intent = new Intent(getContext(), SetupPwdActivity.class);
            intent.putExtra("action", "change");
            startActivity(intent);
        });
        shop.setOnClickListener(view ->{
            Intent intent = new Intent(getContext(), AddShopActivity.class);
            startActivity(intent);
        });
        advanced.setOnClickListener(view ->{

            Bundle b = new Bundle();
            b.putString("shopId",shopId);
            b.putString("shopName",shopName);
            Intent i = new Intent(getContext(), AdvancedActivity.class);
            i.putExtras(b);
            startActivity(i);
        });
        reallocate.setOnClickListener(view ->{

            Bundle b = new Bundle();
            b.putString("shopId",shopId);
            b.putString("shopName",shopName);
            Intent i = new Intent(getContext(), ReallocateItemActivity.class);
            i.putExtras(b);
            startActivity(i);
        });
        importExcel.setOnClickListener(view ->{
            askPermissionAndBrowseFile();
            showFileChooser();

        });


        // Initialize sign in client
        googleSignInClient = GoogleSignIn.getClient(getContext(), GoogleSignInOptions.DEFAULT_SIGN_IN);
        btLogout.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Logging out");
            builder.setMessage("Are you sure?");
            builder.setPositiveButton("OK", (dialog, which) -> {
                // Sign out from google
                googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Check condition
                        if (task.isSuccessful()) {
                            // When task is successful sign out from firebase
                            firebaseAuth.signOut();
                            startActivity(new Intent(getContext(), LoginActivity.class));
                            // Display Toast
                            Toast.makeText(getContext(), "Logout successful", Toast.LENGTH_SHORT).show();
                            // Finish activity
                            getActivity().finish();
                        }
                    }
                });
            });
            builder.setNegativeButton("CANCEL",((dialog, which) -> {
            }));
            AlertDialog dialog = builder.create();
            dialog.show();


        });

        return root;
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


    private void askPermissionAndBrowseFile()  {
        // With Android Level >= 23, you have to ask the user
        // for permission to access External Storage.
        if (ActivityCompat.checkSelfPermission(getContext(), WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{WRITE_EXTERNAL_STORAGE}, 1);
            return;
        }
    }

    public void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/xlsx");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If the user doesn't pick a file just return
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_CODE || resultCode != RESULT_OK) {
            return;
        }

        // Import the file
        importFile(data.getData());
    }

    public void importFile(Uri uri) {
        String fileName = getFileName(uri);
        String realUri = getRealPathFromURI(uri);

        filePath = Uri.parse(realUri);
        Bundle b = new Bundle();
        b.putString("shopId",shopId);
        b.putString("shopName",shopName);
        b.putString("path", String.valueOf(filePath));
        Intent i = new Intent(getContext(), AddItemsFromExcelActivity.class);
        i.putExtras(b);
        startActivity(i);

    }

    /**
     * Obtains the file name for a URI using content resolvers. Taken from the following link
     * https://developer.android.com/training/secure-file-sharing/retrieve-info.html#RetrieveFileInfo
     *
     * @param uri a uri to query
     * @return the file name with no path
     * @throws IllegalArgumentException if the query is null, empty, or the column doesn't exist
     */
    private String getFileName(Uri uri) throws IllegalArgumentException {
        // Obtain a cursor with information regarding this uri
        Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);

        if (cursor.getCount() <= 0) {
            cursor.close();
            throw new IllegalArgumentException("Can't obtain file name, cursor is empty");
        }

        cursor.moveToFirst();

        String fileName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));

        cursor.close();

        return fileName;
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getActivity().getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

}