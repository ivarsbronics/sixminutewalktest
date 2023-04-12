package com.ivarsbronics.sixminutewalktest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ivarsbronics.sixminutewalktest.databinding.ActivityProfileBinding;
import com.ivarsbronics.sixminutewalktest.databinding.ActivityTestResultsBinding;

import java.sql.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class TestResultsActivity extends DrawerBaseActivity {

    private static final String dbInstance = "https://sixminutewalktest-ff14a-default-rtdb.europe-west1.firebasedatabase.app";
    private static final String TAG = "TestResultsActivity";

    ActivityTestResultsBinding activityTestResultsBinding;

    private FirebaseAuth mAuth;
    private HashMap<String, String> hrMap = new HashMap();
    private double totalDistance, estimatedDistance;
    private long testStartMillis;

    private String gender, bd, testResults, testEndPrematurely; //, height, weight;

    private TextView txtTestInfo;

    int height, weight;

    private int age, maxHR, averageHR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_test_results);
        activityTestResultsBinding = ActivityTestResultsBinding.inflate(getLayoutInflater());
        setContentView(activityTestResultsBinding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        txtTestInfo = findViewById(R.id.txtTestInfo);

        Intent intent = getIntent();
        hrMap = (HashMap<String, String>) intent.getSerializableExtra("EXTRA_HR_MAP");
        totalDistance = (double) intent.getSerializableExtra("EXTRA_DISTANCE");
        testStartMillis = (long) intent.getSerializableExtra("EXTRA_TEST_ID");
        testEndPrematurely = (String) intent.getSerializableExtra("EXTRA_END_PREMATURELY");


        DatabaseReference databaseReference = FirebaseDatabase.getInstance(dbInstance).getReference("Users").child(currentUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Log.d(TAG, "###SNAPSHOT: " + dataSnapshot.getKey() + ": " + dataSnapshot.getValue());
                    if ("gender".equals(dataSnapshot.getKey())) {
                        if("Male".equals(dataSnapshot.getValue())){
                            gender = "Male";
                        }
                        else if("Female".equals(dataSnapshot.getValue())){
                            gender = "Female";
                        }
                        else{
                            //Toast.makeText(TestResultsActivity.this, "Error loading Gender value - please mark gender and re-save test parameters", Toast.LENGTH_LONG).show();
                        }
                    }
                    if("height".equals(dataSnapshot.getKey())){
                        height = Integer.valueOf((String) dataSnapshot.getValue());
                    }
                    if("weight".equals(dataSnapshot.getKey())){
                        weight = Integer.valueOf((String) dataSnapshot.getValue());
                    }
                    if("birthDate".equals(dataSnapshot.getKey())){
                        bd = String.valueOf(dataSnapshot.getValue()).replace(" ", "-");
                        SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
                        //Date birthDate;
                        try {
                            /*translate birth date to calendar object*/
                            Date birthDate = format.parse(bd);
                            Log.d(TAG, "birthDate = " + birthDate);
                            Calendar calBirthDate = Calendar.getInstance();
                            calBirthDate.setTimeInMillis(birthDate.getTime());
                            Log.d(TAG, "calBirthDate = " + calBirthDate);

                            /*create calendar object for current day*/
                            long currentTime = System.currentTimeMillis();
                            Log.d(TAG, "currentTime = " + currentTime);
                            Calendar calNow = Calendar.getInstance();
                            calNow.setTimeInMillis(currentTime);
                            Log.d(TAG, "calNow = " + calNow);
                            age = calNow.get(Calendar.YEAR) - calBirthDate.get(Calendar.YEAR);
                            Log.d(TAG, "age = " + age);

                            /*age value postprocessing*/
                            int currMonth = calNow.get(Calendar.MONTH) + 1;
                            int birthMonth = calBirthDate.get(Calendar.MONTH) + 1;
                            int months = currMonth - birthMonth;
                            if ((months < 0) || (months == 0 && calNow.get(Calendar.DATE) < calBirthDate.get(Calendar.DATE))){
                                age --;
                            }
                            /*https://journals.lww.com/acsm-msse/Fulltext/2007/05000/Longitudinal_Modeling_of_the_Relationship_between.11.aspx*/
                            maxHR = (int) (207 - 0.7 * age);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

                Set<String> timeKeys = hrMap.keySet();
                ArrayList<String> listOfTimeKeys = new ArrayList<>(timeKeys);
                ArrayList<Integer> integerListOfTimeKeys = new ArrayList<>();
                Collection<String> hrValues = hrMap.values();
                ArrayList<String> listOfHRValues = new ArrayList<>(hrValues);
                ArrayList<Integer> integerListOfHRValues = new ArrayList<>();
                Iterator iterator = listOfHRValues.iterator();
                int count = 0;
                int sum = 0;
                while (iterator.hasNext()) {
                    int hrValue = Integer.valueOf((String) iterator.next());
                    integerListOfHRValues.add(hrValue);
                    count = count + 1;
                    sum = sum + hrValue;
                }
                iterator = listOfTimeKeys.iterator();
                while (iterator.hasNext()) {
                    int time = Integer.valueOf((String) iterator.next());
                    integerListOfTimeKeys.add(time);
                }


                averageHR = (int) (sum/count);

                /*https://academic.oup.com/eurjcn/article/8/1/2/5929208*/
                if (gender == "Female") {
                    estimatedDistance = (2.11 * height) - (2.29 * weight) - (5.78 * age) + 667;
                }
                else if (gender == "Male") {
                    estimatedDistance = (7.75 * height) - (5.02 * age) - (1.76 * weight) - 309;
                }

                testResults = "According to provided test parameters estimated distance for healthy" +
                        " individual is:\n\t" + estimatedDistance + " meters.\nActual distance covered in 6 minutes" +
                        " of the test based on GPS tracking is:\n\t" + totalDistance + " meters. \nDuring test you have covered " +
                        totalDistance/estimatedDistance*100 + "% of estimated distance.\n" +
                        "Your estimated maximum Heart Rate according to formula \"(207 - 0.7 * Age)\"" +
                        "is:\n\t" + maxHR + "\n Your average Heart Rate during est was:\n\t" + averageHR;

                txtTestInfo.setText(testResults);
                Log.d(TAG, "gender = " + gender);
                Log.d(TAG, "bd = " + bd);
                Log.d(TAG, "age = " + age);
                Log.d(TAG, "height = " + height);
                Log.d(TAG, "weight = " + weight);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private int getMonthNumber(String month) {
        if ("JAN".equals(month)){
            return 1;
        }
        if ("FEB".equals(month)){
            return 2;
        }
        if ("MAR".equals(month)){
            return 3;
        }
        if ("APR".equals(month)){
            return 4;
        }
        if ("MAY".equals(month)){
            return 5;
        }
        if ("JUN".equals(month)){
            return 6;
        }
        if ("JUL".equals(month)){
            return 7;
        }
        if ("AUG".equals(month)){
            return 8;
        }
        if ("SEP".equals(month)){
            return 9;
        }
        if ("OCT".equals(month)){
            return 10;
        }
        if ("NOV".equals(month)){
            return 11;
        }
        if ("DEC".equals(month)){
            return 12;
        }

        //in case of unexpected text return -1

        Log.d(TAG, "" + month);
        return -1;
    }
}