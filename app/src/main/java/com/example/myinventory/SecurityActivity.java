package com.example.myinventory;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.example.myinventory.R;
import com.example.myinventory.ui.dashboard.DashboardFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.Executor;

public class SecurityActivity extends AppCompatActivity {
    EditText pwd;
    int counter=0;
    private FirebaseFirestore db;
    SharedPreferences sh;
    int shopsCount;
    String shopId="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sh = getSharedPreferences("pref", Context.MODE_PRIVATE);
        setContentView(R.layout.activity_security);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        db = FirebaseFirestore.getInstance();

        shopsCount = sh.getInt("shopCount",0);

//        db.collection("Shops").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//            @Override
//            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                shopsCount = queryDocumentSnapshots.size();
//
//            }
//        });

        // Initialising msgtext and loginbutton
        pwd = findViewById(R.id.pwd);
        ImageButton loginbutton = findViewById(R.id.login);

        // creating a variable for our BiometricManager
        // and lets check if our user can use biometric sensor or not
        BiometricManager biometricManager = androidx.biometric.BiometricManager.from(this);
        switch (biometricManager.canAuthenticate()) {

            // this means we can use biometric sensor
            case BiometricManager.BIOMETRIC_SUCCESS:
//                Toast.makeText(this, "You can use the fingerprint sensor to login", Toast.LENGTH_SHORT).show();
                break;

            // this means that the device doesn't have fingerprint sensor
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(this, "This device does not have a fingerprint sensor", Toast.LENGTH_SHORT).show();
                break;

            // this means that biometric sensor is not available
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(this, "The biometric sensor is currently unavailable", Toast.LENGTH_SHORT).show();
                break;

            // this means that the device doesn't contain your fingerprint
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(this, "Your device doesn't have fingerprint saved,please check your security settings", Toast.LENGTH_SHORT).show();
                break;
        }
        // creating a variable for our Executor
        Executor executor = ContextCompat.getMainExecutor(this);
        // this will give us result of AUTHENTICATION
        final BiometricPrompt biometricPrompt = new BiometricPrompt(SecurityActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
            }

            // THIS METHOD IS CALLED WHEN AUTHENTICATION IS SUCCESS
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
//                Toast.makeText(getApplicationContext(), "Login Success", Toast.LENGTH_SHORT).show();

                if(shopsCount == 0) {
                    Intent i=new Intent(SecurityActivity.this, AddShopActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }else{
                    Intent i=new Intent(SecurityActivity.this, ShopSelectorActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
            }
            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });
        // creating a variable for our promptInfo
        // BIOMETRIC DIALOG
        final BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder().setTitle("Biometrics")
                .setDescription("Use fingerprint to login ").setNegativeButtonText("Cancel").build();
        loginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                biometricPrompt.authenticate(promptInfo);
            }
        });
        Boolean fp = sh.getBoolean("fp", false);

        if(fp){
            biometricPrompt.authenticate(promptInfo);
        }else{
            loginbutton.setOnClickListener(null);
        }
    }

    public void pwdInput(View view) {
        String s1 = sh.getString("password", "");
        String text = pwd.getText().toString();

        if(view.getId() != R.id.backSpace){
            Button input = (Button) view;
            counter++;
            if(counter <= 6){
                pwd.setText(text+input.getText());
            }
            if(counter == 6){
                if(s1.trim().equals(pwd.getText().toString())){
                    if(shopsCount == 0) {
                        startActivity(new Intent(SecurityActivity.this, AddShopActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }else{
                        startActivity(new Intent(SecurityActivity.this, ShopSelectorActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                }else{
                    counter = 0;
                    pwd.setText("");
                    Toast.makeText(this, "Wrong password", Toast.LENGTH_SHORT).show();
                }
            }

        }else{
            if(text.length() > 0){
                text = text.substring(0, text.length() - 1);
                pwd.setText(text);
                counter--;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
        finish();
    }
}
