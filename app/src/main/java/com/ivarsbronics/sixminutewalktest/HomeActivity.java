package com.ivarsbronics.sixminutewalktest;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ivarsbronics.sixminutewalktest.databinding.ActivityHomeBinding;

public class HomeActivity extends DrawerBaseActivity{

    ActivityHomeBinding activityHomeBinding;

    private static final String TAG = "HomeActivity";

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private TextView txtDisclaimerDetails;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityHomeBinding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(activityHomeBinding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        txtDisclaimerDetails = findViewById(R.id.txtDisclaimerDetails);
        txtDisclaimerDetails.setMovementMethod(new ScrollingMovementMethod());

    }
}