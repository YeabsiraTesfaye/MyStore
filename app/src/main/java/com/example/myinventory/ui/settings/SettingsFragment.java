package com.example.myinventory.ui.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.example.myinventory.AdvancedActivity;
import com.example.myinventory.LoginActivity;
import com.example.myinventory.R;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsFragment extends Fragment {
    FragmentSettingsBinding binding;
    CircleImageView civ;
    TextView name, email;
    Button btLogout,changePwd, advanced;
    ToggleButton toggleButton;
    FirebaseAuth firebaseAuth;
    GoogleSignInClient googleSignInClient;
    SharedPreferences sh;
    ConstraintLayout cl;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        sh  = getActivity().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        binding =  FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        toggleButton = root.findViewById(R.id.toggleButton);
        civ = root.findViewById(R.id.circleImageView);
        name = root.findViewById(R.id.name);
        email = root.findViewById(R.id.mail);
        btLogout = root.findViewById(R.id.logoutBtn);
        changePwd = root.findViewById(R.id.change);
        advanced = root.findViewById(R.id.transaction);
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
        advanced.setOnClickListener(view ->{
            startActivity(new Intent(getContext(), AdvancedActivity.class));
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

}