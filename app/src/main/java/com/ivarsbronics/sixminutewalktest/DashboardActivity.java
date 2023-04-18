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
import android.widget.TextView;
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

    private TextView txtInfo;

    private ArrayList<TestInfo> testInfoArrayList = new ArrayList<>();
    private TestListAdapter testListAdapter;

    private int testCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        activityDashboardBinding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(activityDashboardBinding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        txtInfo = findViewById(R.id.txtInfo);

        DatabaseReference userTestsReference = FirebaseDatabase.getInstance(dbInstance).getReference("tests").child(currentUser.getUid());
        userTestsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                testInfoArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    TestInfo testInfo = dataSnapshot.getValue(TestInfo.class);
                    testInfoArrayList.add(testInfo);
                    testCount++;
                }

                if (testCount > 0) {
                    testListAdapter = new TestListAdapter(DashboardActivity.this, testInfoArrayList);
                    activityDashboardBinding.testsListView.setAdapter(testListAdapter);

                    activityDashboardBinding.testsListView.setClickable(true);
                    activityDashboardBinding.testsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            Intent intent = new Intent(DashboardActivity.this, TestInfoActivity.class);
                            intent.putExtra("EXTRA_TEST_INFO", testInfoArrayList.get(i));
                            startActivity(intent);
                        }
                    });
                }
                else {
                    txtInfo.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}