package com.ivarsbronics.sixminutewalktest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ivarsbronics.sixminutewalktest.databinding.ActivityHomeBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HomeActivity extends DrawerBaseActivity{

    ActivityHomeBinding activityHomeBinding;

    private static final String TAG = "HomeActivity";

    private static final String dbInstance = "https://sixminutewalktest-ff14a-default-rtdb.europe-west1.firebasedatabase.app";
    private FirebaseAuth mAuth;
    private Button btnLogOut, btnStartTest, btnToDashboard;
    private FirebaseUser currentUser;
    private TextView txtDisclaimerDetails;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityHomeBinding = ActivityHomeBinding.inflate(getLayoutInflater());
        //setContentView(R.layout.activity_home);
        setContentView(activityHomeBinding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        btnStartTest = findViewById(R.id.btnStartTest);
        btnToDashboard = findViewById(R.id.btnToDashboard);
        txtDisclaimerDetails = findViewById(R.id.txtDisclaimerDetails);



        /*btnStartTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToTest();
            }
        });

        btnToDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToDashboard();
            }
        });*/
    }

    public void goToTest() {
        startActivity(new Intent(HomeActivity.this, SixMWTActivity.class));
    }

    public void goToDashboard() {
        startActivity(new Intent(HomeActivity.this, DashboardActivity.class));
    }
}