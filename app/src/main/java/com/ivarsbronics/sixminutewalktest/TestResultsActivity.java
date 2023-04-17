package com.ivarsbronics.sixminutewalktest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.Collections;
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
    private HashMap<String, String> testParameters = new HashMap();
    private HashMap<String, String> addTestParameters = new HashMap();
    private double totalDistance, estimatedDistance;
    private long testStartMillis;

    private String gender, bd, testResults, testEndPrematurely; //, height, weight;

    private TextView txtTestInfo;

    int height, weight, hrBelowZone1, hrZone1, hrZone2, hrZone3, hrZone4, hrZone5, hrAboveZone5;

    private int age, maxHR, averageHR;

    private boolean useUserEnteredDistance = false;

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
        testParameters = (HashMap<String, String>) intent.getSerializableExtra("EXTRA_TEST_PARAMETERS");
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
                hrBelowZone1 = 0;
                hrZone1 = 0;
                hrZone2 = 0;
                hrZone3 = 0;
                hrZone4 = 0;
                hrZone5 = 0;
                hrAboveZone5 = 0;
                while (iterator.hasNext()) {
                    int hrValue = Integer.valueOf((String) iterator.next());
                    integerListOfHRValues.add(hrValue);
                    count = count + 1;
                    sum = sum + hrValue;
                    if (hrValue < maxHR * 0.5) {
                        hrBelowZone1 = hrBelowZone1 + 1;
                    }
                    else if (hrValue >= maxHR * 0.5 && hrValue < maxHR * 0.6 ) {
                        hrZone1 = hrZone1 + 1;
                    }
                    else if (hrValue >= maxHR * 0.6 && hrValue < maxHR * 0.7 ) {
                        hrZone2 = hrZone2 + 1;
                    }
                    else if (hrValue >= maxHR * 0.7 && hrValue < maxHR * 0.8 ) {
                        hrZone3 = hrZone3 + 1;
                    }
                    else if (hrValue >= maxHR * 0.8 && hrValue < maxHR * 0.9 ) {
                        hrZone3 = hrZone4 + 1;
                    }
                    else if (hrValue >= maxHR * 0.9 && hrValue <= maxHR ) {
                        hrZone5 = hrZone5 + 1;
                    }
                    else if (hrValue > maxHR) {
                        hrAboveZone5 = hrAboveZone5 + 1;
                    }
                }
                iterator = listOfTimeKeys.iterator();
                while (iterator.hasNext()) {
                    int time = Integer.valueOf((String) iterator.next());
                    integerListOfTimeKeys.add(time);
                }


                if (count > 0) {
                    averageHR = (int) (sum / count);
                    /*use same variables for holding percentage in zones*/
                    hrBelowZone1 = (int) (hrBelowZone1 / count * 100);
                    hrZone1 = (int) (hrZone1 / count * 100);
                    hrZone2 = (int) (hrZone2 / count * 100);
                    hrZone3 = (int) (hrZone3 / count * 100);
                    hrZone4 = (int) (hrZone4 / count * 100);
                    hrZone5 = (int) (hrZone5 / count * 100);
                    hrAboveZone5 = (int) (hrAboveZone5 / count * 100);
                }

                /*https://academic.oup.com/eurjcn/article/8/1/2/5929208*/
                if (gender == "Female") {
                    estimatedDistance = (2.11 * height) - (2.29 * weight) - (5.78 * age) + 667;
                }
                else if (gender == "Male") {
                    estimatedDistance = (7.75 * height) - (5.02 * age) - (1.76 * weight) - 309;
                }

                addTestParameters.put("gender", gender);
                addTestParameters.put("age", String.valueOf(age));
                addTestParameters.put("height", String.valueOf(height));
                addTestParameters.put("weight", String.valueOf(weight));
                addTestParameters.put("hrBelowZone1", String.valueOf(hrBelowZone1));
                addTestParameters.put("hrZone1", String.valueOf(hrZone1));
                addTestParameters.put("hrZone2", String.valueOf(hrZone2));
                addTestParameters.put("hrZone3", String.valueOf(hrZone3));
                addTestParameters.put("hrZone4", String.valueOf(hrZone4));
                addTestParameters.put("hrZone5", String.valueOf(hrZone5));
                addTestParameters.put("hrAboveZone5", String.valueOf(hrAboveZone5));
                addTestParameters.put("estimatedDistance", String.valueOf(estimatedDistance));
                addTestParameters.put("averageHeartRate", String.valueOf(averageHR));

                DatabaseReference testsReference = FirebaseDatabase.getInstance(dbInstance).getReference("tests");
                testsReference.child(currentUser.getUid()).child(String.valueOf(testStartMillis)).child("testParameters").updateChildren(Collections.unmodifiableMap(addTestParameters)).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //Toast.makeText(SixMWTActivity.this, "Location Data Saved", Toast.LENGTH_LONG).show();
                            Log.d(TAG, "### ADDITIONAL PARAMETERS DATA SAVED!!!!!");
                        } else {
                            Log.d(TAG, task.getException().getMessage());
                            //Toast.makeText(SixMWTActivity.this, "Location Data NOT SAVED", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                if (!String.valueOf(totalDistance).equals(testParameters.get("userTotalDistance"))){
                    totalDistance = Double.valueOf(testParameters.get("userTotalDistance"));
                    useUserEnteredDistance = true;
                }

                testResults = "According to provided test parameters estimated distance for healthy" +
                        " individual is:\n\t\t" + estimatedDistance + " meters.\nActual distance covered in 6 minutes" +
                        " of the test ";
                if (useUserEnteredDistance) {
                    testResults = testResults + "(entered by user) is:\n\t\t";
                }
                else {
                    testResults = testResults + "based on GPS tracking is:\n\t\t";
                }
                testResults = testResults + totalDistance + " meters. \nDuring test you have covered " +
                        (double)((int)(totalDistance/estimatedDistance*10000))/100 + "% of estimated distance.\n\n" +
                        "Your estimated maximum Heart Rate according to formula \"(207 - 0.7 * Age)\"" +
                        "is:\n\t\t" + maxHR + "\n";
                if (Integer.valueOf(testParameters.get("prepPhaseHRMin")) != null && !"".equals(testParameters.get("prepPhaseHRMin"))) {
                    if (Integer.valueOf(testParameters.get("prepPhaseHRMin")) > 0) {
                        testResults = testResults + "Minimal registered heart rate during preparation phase was:\n\t\t" + testParameters.get("prepPhaseHRMin") + "\n";
                    }
                }
                if (count > 0) {
                    testResults = testResults + "Your average Heart Rate during test was:\n\t\t" + averageHR + "\n\n" +
                            "Time spent in Heart Rate zones:\n" +
                            "\t\tBelow Zone1: \t" + hrBelowZone1 + "%\n" +
                            "\t\tZone 1 (50% - 60%):\t" + hrZone1 + "%\n" +
                            "\t\tZone 2 (60% - 70%):\t" + hrZone2 + "%\n" +
                            "\t\tZone 3 (70% - 80%):\t" + hrZone3 + "%\n" +
                            "\t\tZone 4 (80% - 90%):\t" + hrZone4 + "%\n" +
                            "\t\tZone 5 (90% - 100%):\t" + hrZone5 + "%\n" +
                            "\t\tAbove Zone 5:\t\t" + hrAboveZone5 + "%\n\n";
                }
                testResults = testResults + "User parameters:\n" +
                        "\t\tGender: " + gender + "\n" +
                        "\t\tAge: " + age + "\n" +
                        "\t\tHeight: " + height + "\n" +
                        "\t\tWeight: " + weight + "\n\n" +
                        "Pre-test measurements:\n" +
                        "\t\tDyspnea: " + testParameters.get("preTestValueDyspnea") + "\n" +
                        "\t\tFatigue: " + testParameters.get("preTestValueFatigue") + "\n" +
                        "\t\tBood Pressure:\n" +
                        "\t\t\tSystolic: " + testParameters.get("preTestBloodPressureSystolic") + "\n" +
                        "\t\t\tDiastolic: " + testParameters.get("preTestBloodPressureDiastolic") + "\n" +
                        "\t\tOxygen Saturation: " + testParameters.get("preTestOxygenSaturation") + "\n\n" +
                        "Post-test measurements:\n" +
                        "\t\tDyspnea: " + testParameters.get("postTestValueDyspnea") + "\n" +
                        "\t\tFatigue: " + testParameters.get("postTestValueFatigue") + "\n" +
                        "\t\tBood Pressure:\n" +
                        "\t\t\tSystolic: " + testParameters.get("postTestBloodPressureSystolic") + "\n" +
                        "\t\t\tDiastolic: " + testParameters.get("postTestBloodPressureDiastolic") + "\n" +
                        "\t\tOxygen Saturation: " + testParameters.get("postTestOxygenSaturation") + "\n";

                txtTestInfo.setMovementMethod(new ScrollingMovementMethod());
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