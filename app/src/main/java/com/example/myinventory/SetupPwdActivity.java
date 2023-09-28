package com.example.myinventory;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class SetupPwdActivity extends AppCompatActivity {
    TextView pwd,confirmPwd,currentPwd;
    Button setPwd;
    TextView info;
    ToggleButton togglebutton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_pwd);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref",MODE_PRIVATE);
        currentPwd = findViewById(R.id.currentPwd);
        pwd = findViewById(R.id.pwd);
        confirmPwd = findViewById(R.id.confirmPwd);
        setPwd = findViewById(R.id.setPwd);
        info = findViewById(R.id.info);
        togglebutton
                = findViewById(
                R.id.toggleButton);

        Intent intent = getIntent();
        // receive the value by getStringExtra() method and
        // key must be same which is send by first activity
        String str = intent.getStringExtra("action");
        if(str.equals("setup")){
            info.setText("CREATE PASSWORD");
            currentPwd.setVisibility(View.GONE);
            setPwd.setOnClickListener(v -> {
                String pwdS = pwd.getText().toString().trim();
                String confirmPwdS = confirmPwd.getText().toString().trim();
                if(!pwdS.equals(confirmPwdS)){
                    Toast.makeText(this, "Password and Confirm password must be similar!", Toast.LENGTH_SHORT).show();
                }else if(pwdS.length() < 6 || confirmPwdS.length() < 6){
                    Toast.makeText(this, "Password must be 6 digits!", Toast.LENGTH_SHORT).show();
                }else{
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();
                    myEdit.putString("password", pwdS);
                    myEdit.commit();

                    if(str.equals("change") || str.equals("setup")){
                        SharedPreferences sh = getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
                        String s1 = sh.getString("password", "");
                        if(!s1.trim().equals("")){
                            startActivity(new Intent(SetupPwdActivity.this, SecurityActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        }
                    }
                }
            });
        }else if(str.equals("change")){
            info.setText("CHANGE PASSWORD");
            currentPwd.setVisibility(View.VISIBLE);
            Boolean fp = sharedPreferences.getBoolean("fp",false);
            togglebutton.setChecked(fp);
            setPwd.setOnClickListener(v -> {
                String pwdS = pwd.getText().toString().trim();
                String confirmPwdS = confirmPwd.getText().toString().trim();
                if(!sharedPreferences.getString("password","").equals(currentPwd.getText().toString())){
                    Toast.makeText(this, "Wrong Current Password!", Toast.LENGTH_SHORT).show();

                }
                else if(!pwdS.equals(confirmPwdS)){
                    Toast.makeText(this, "Password and Confirm password must be similar!", Toast.LENGTH_SHORT).show();
                }else if(pwdS.length() < 6 || confirmPwdS.length() < 6){
                    Toast.makeText(this, "Password must be 6 digits!", Toast.LENGTH_SHORT).show();
                }else{

                    SharedPreferences.Editor myEdit = sharedPreferences.edit();
                    myEdit.putString("password", pwdS);
                    myEdit.commit();

                    if(str.equals("change") || str.equals("setup")){
                        SharedPreferences sh = getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
                        String s1 = sh.getString("password", "");
                        if(!s1.trim().equals("")){
                            startActivity(new Intent(SetupPwdActivity.this, SecurityActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        }
                    }




                }
            });
        }




    }


    public void onToggleClick(View view) {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref",MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();


        myEdit.putBoolean("fp", togglebutton.isChecked());
        myEdit.commit();
    }
}