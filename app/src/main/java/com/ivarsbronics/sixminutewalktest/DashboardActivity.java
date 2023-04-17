package com.ivarsbronics.sixminutewalktest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ivarsbronics.sixminutewalktest.databinding.ActivityDashboardBinding;

import java.util.ArrayList;

public class DashboardActivity extends DrawerBaseActivity {

    private static final String TAG = "DashboardActivity";

    private static final String dbInstance = "https://sixminutewalktest-ff14a-default-rtdb.europe-west1.firebasedatabase.app";

    ActivityDashboardBinding activityDashboardBinding;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private String userTotalDistance, totalDistance;

    private ArrayList<TestInfo> testInfoArrayList = new ArrayList<>();
    private TestListAdapter testListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        activityDashboardBinding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(activityDashboardBinding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        DatabaseReference userTestsReference = FirebaseDatabase.getInstance(dbInstance).getReference("Tests").child(currentUser.getUid());
        userTestsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    TestInfo testInfo = new TestInfo();
                    Log.d(TAG, "###SNAPSHOT: " + dataSnapshot.getKey() + ": " + dataSnapshot.getValue());
                    if ("testDateTime".equals(dataSnapshot.getKey())) {
                        testInfo.setTestDateTime((String) dataSnapshot.getValue());
                    }
                    if("userTotalDistance".equals(dataSnapshot.getKey())){
                        testInfo.setUserTotalDistance((String) dataSnapshot.getValue());
                    }
                    if("totalDistance".equals(dataSnapshot.getKey())){
                        testInfo.setTotalDistance((String) dataSnapshot.getValue());
                    }
                    if("averageHR".equals(dataSnapshot.getKey())){
                        testInfo.setAverageHR((String) dataSnapshot.getValue());
                    }
                    testInfoArrayList.add(testInfo);
                }

                testListAdapter = new TestListAdapter(DashboardActivity.this, testInfoArrayList);
                activityDashboardBinding.testsListView.setAdapter(testListAdapter);

                activityDashboardBinding.testsListView.setClickable(true);
                activityDashboardBinding.testsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        //Intent intent = new Intent(DashboardActivity.this, TestInfoActivity.class);
                        //intent.putExtra("EXTRA_TEST_ID", testID);
                        //startActivity(intent);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}