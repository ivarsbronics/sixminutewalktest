package com.ivarsbronics.sixminutewalktest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class TestInfoActivity extends AppCompatActivity {
    /*logging parameter*/
    private static final String TAG = "TestInfoActivity";

    private static final String dbInstance = "https://sixminutewalktest-ff14a-default-rtdb.europe-west1.firebasedatabase.app";

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private TestInfo testInfo;
    private TextView txtTestInfo;
    private Button btnBack;
    private double totalDistance, estimatedDistance;
    private boolean useUserEnteredDistance;
    private String testResults;

    private double hrBelowZone1Percent = 0;
    private double hrZone1Percent = 0;
    private double hrZone2Percent = 0;
    private double hrZone3Percent = 0;
    private double hrZone4Percent = 0;
    private double hrZone5Percent = 0;
    private double hrAboveZone5Percent = 0;
    private double estimatedMaxHR = 0;

    private int hrBelowZone1 = 0;
    private int hrZone1 = 0;
    private int hrZone2 = 0;
    private int hrZone3 = 0;
    private int hrZone4 = 0;
    private int hrZone5 = 0;
    private int hrAboveZone5 = 0;
    private int hrValueCount = 0;

    HashMap<String, Object> testInfoUpdate = new HashMap<>();

    private long testStartMillis;

    private DecimalFormat df = new DecimalFormat("#0.00");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_info);

        txtTestInfo = findViewById(R.id.txtTestInfo);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        Intent intent = getIntent();
        testInfo = (TestInfo)intent.getParcelableExtra("EXTRA_TEST_INFO");
        //localHRMap = (HashMap<String, String>) intent.getSerializableExtra("EXTRA_HR_MAP");
        testInfo.setHrMap((HashMap<String, String>) intent.getSerializableExtra("EXTRA_HR_MAP"));
        testStartMillis = Long.parseLong(testInfo.getTestTimeInMillis());

        Log.d(TAG, "## TestInfoActivity: getHRMap()" + testInfo.getHrMap());

        if ((testInfo.getDeviceName() != null && !"".equals(testInfo.getDeviceName())) || !"Y".equals(testInfo.getHrMonitorSkipped())) {

            if (testInfo.getHrBelowZone1Percent() != null && !"".equals(testInfo.getHrBelowZone1Percent())) {
                hrBelowZone1Percent = Double.parseDouble(testInfo.getHrBelowZone1Percent());
            }
            if (testInfo.getHrZone1Percent() != null && !"".equals(testInfo.getHrZone1Percent())) {
                hrZone1Percent = Double.parseDouble(testInfo.getHrZone1Percent());
            }
            if (testInfo.getHrZone2Percent() != null && !"".equals(testInfo.getHrZone2Percent())) {
                hrZone2Percent = Double.parseDouble(testInfo.getHrZone2Percent());
            }
            if (testInfo.getHrZone3Percent() != null && !"".equals(testInfo.getHrZone3Percent())) {
                hrZone3Percent = Double.parseDouble(testInfo.getHrZone3Percent());
            }
            if (testInfo.getHrZone4Percent() != null && !"".equals(testInfo.getHrZone4Percent())) {
                hrZone4Percent = Double.parseDouble(testInfo.getHrZone4Percent());
            }
            if (testInfo.getHrZone5Percent() != null && !"".equals(testInfo.getHrZone5Percent())) {
                hrZone5Percent = Double.parseDouble(testInfo.getHrZone5Percent());
            }
            if (testInfo.getHrAboveZone5Percent() != null && !"".equals(testInfo.getHrAboveZone5Percent())) {
                hrAboveZone5Percent = Double.parseDouble(testInfo.getHrAboveZone5Percent());
            }

            if (hrBelowZone1Percent + hrZone1Percent + hrZone2Percent + hrZone3Percent + hrZone4Percent + hrZone5Percent + hrAboveZone5Percent < 91) {
                if (testInfo.getHrMaxByFormula() != null && !"".equals(testInfo.getHrMaxByFormula())) {
                    estimatedMaxHR = Double.parseDouble(testInfo.getHrMaxByFormula());
                }
                if (estimatedMaxHR > 0) {
                    for (Object hrValue : testInfo.getHrMap().values()) {
                        int value = Integer.parseInt(String.valueOf(hrValue));
                        //Log.d(TAG, "estimatedMaxHR = " + estimatedMaxHR);
                        Log.d(TAG, "hrValue = " + hrValue);
                        hrValueCount = hrValueCount + 1;
                        Log.d(TAG, "hrValueCount = " + hrValueCount);
                        if (value < estimatedMaxHR * 0.5) {
                            hrBelowZone1 = hrBelowZone1 + 1;
                        } else if (value >= estimatedMaxHR * 0.5 && value < estimatedMaxHR * 0.6) {
                            hrZone1 = hrZone1 + 1;
                        } else if (value >= estimatedMaxHR * 0.6 && value < estimatedMaxHR * 0.7) {
                            hrZone2 = hrZone2 + 1;
                        } else if (value >= estimatedMaxHR * 0.7 && value < estimatedMaxHR * 0.8) {
                            hrZone3 = hrZone3 + 1;
                        } else if (value >= estimatedMaxHR * 0.8 && value < estimatedMaxHR * 0.9) {
                            hrZone3 = hrZone4 + 1;
                        } else if (value >= estimatedMaxHR * 0.9 && value <= estimatedMaxHR) {
                            hrZone5 = hrZone5 + 1;
                        } else if (value > estimatedMaxHR) {
                            hrAboveZone5 = hrAboveZone5 + 1;
                        }
                    }
                }

                hrBelowZone1Percent = (double) ((int) ((double) hrBelowZone1 / (double) hrValueCount * 10000)) / 100;
                hrZone1Percent = (double) ((int) ((double) hrZone1 / (double) hrValueCount * 10000)) / 100;
                hrZone2Percent = (double) ((int) ((double) hrZone2 / (double) hrValueCount * 10000)) / 100;
                hrZone3Percent = (double) ((int) ((double) hrZone3 / (double) hrValueCount * 10000)) / 100;
                hrZone4Percent = (double) ((int) ((double) hrZone4 / (double) hrValueCount * 10000)) / 100;
                hrZone5Percent = (double) ((int) ((double) hrZone5 / (double) hrValueCount * 10000)) / 100;
                hrAboveZone5Percent = (double) ((int) ((double) hrAboveZone5 / (double) hrValueCount * 10000)) / 100;

                testInfo.setHrBelowZone1Percent(String.valueOf(hrBelowZone1Percent));
                testInfo.setHrZone1Percent(String.valueOf(hrZone1Percent));
                testInfo.setHrZone2Percent(String.valueOf(hrZone2Percent));
                testInfo.setHrZone3Percent(String.valueOf(hrZone3Percent));
                testInfo.setHrZone4Percent(String.valueOf(hrZone4Percent));
                testInfo.setHrZone5Percent(String.valueOf(hrZone5Percent));
                testInfo.setHrAboveZone5Percent(String.valueOf(hrAboveZone5Percent));

                testInfoUpdate.put("hrBelowZone1Percent", String.valueOf(hrBelowZone1Percent));
                testInfoUpdate.put("hrZone1Percent", String.valueOf(hrZone1Percent));
                testInfoUpdate.put("hrZone2Percent", String.valueOf(hrZone2Percent));
                testInfoUpdate.put("hrZone3Percent", String.valueOf(hrZone3Percent));
                testInfoUpdate.put("hrZone4Percent", String.valueOf(hrZone4Percent));
                testInfoUpdate.put("hrZone5Percent", String.valueOf(hrZone5Percent));
                testInfoUpdate.put("hrAboveZone5Percent", String.valueOf(hrAboveZone5Percent));

                mAuth = FirebaseAuth.getInstance();
                currentUser = mAuth.getCurrentUser();
                DatabaseReference testReference = FirebaseDatabase.getInstance(dbInstance).getReference("tests").child(currentUser.getUid());
                testReference.child(String.valueOf(testStartMillis)).updateChildren(testInfoUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(TestInfoActivity.this, "Updated HR Zone Data based on HR measurements!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        if (testInfo.getTotalDistance().equals(testInfo.getUserTotalDistance())) {
            if (testInfo.getTotalDistance() != null && !"".equals(testInfo.getTotalDistance())) {
                totalDistance = Double.parseDouble(testInfo.getTotalDistance().toString().replace(",","."));
            }
        }
        else {
            if (testInfo.getUserTotalDistance() != null && !"".equals(testInfo.getUserTotalDistance())) {
                totalDistance = Double.parseDouble(testInfo.getUserTotalDistance().toString().replace(",","."));
            }
            useUserEnteredDistance = true;
        }
        Log.d(TAG, "totalDistance = " + totalDistance);
        if (testInfo.getEstimatedDistance() != null && !"".equals(testInfo.getEstimatedDistance())) {
            estimatedDistance = Double.parseDouble(testInfo.getEstimatedDistance().toString().replace(",","."));
        }
        Log.d(TAG, "estimatedDistance = " + estimatedDistance);

        testResults = testInfo.getTestDateTime() + "\n\nAccording to provided test parameters estimated distance for healthy" +
                " individual is:\n\t\t" + df.format(estimatedDistance) + " meters.\nActual distance covered in 6 minutes" +
                " of the test ";
        if (useUserEnteredDistance) {
            testResults = testResults + "(entered by user) is:\n\t\t";
        }
        else {
            testResults = testResults + "based on GPS tracking is:\n\t\t";
        }
        testResults = testResults + df.format(totalDistance) + " meters. \nDuring test you have covered " +
                df.format(totalDistance/estimatedDistance*100) + "% of estimated distance.\n\n" +
                "Your estimated maximum Heart Rate according to formula \"(207 - 0.7 * Age)\"" +
                "is:\n\t\t" + testInfo.getHrMaxByFormula() + "\n";
        if (testInfo.getPrepPhaseHRMin() != null && !"".equals(testInfo.getPrepPhaseHRMin())) {
            if (Integer.parseInt(testInfo.getPrepPhaseHRMin()) > 0) {
                testResults = testResults + "Minimal registered heart rate during preparation phase was:\n\t\t" + testInfo.getPrepPhaseHRMin() + "\n";
            }
        }
        if ("".equals(testInfo.getHrMonitorSkipped()) || testInfo.getHrMonitorSkipped() == null) {
            testResults = testResults + "\nYour average Heart Rate during test was:\n\t\t" + testInfo.getTestAverageHR() + "\n\n" +
                    "Time spent in Heart Rate zones:\n" +
                    "\t\tBelow Zone1: \t" + testInfo.getHrBelowZone1Percent() + "%\n" +
                    "\t\tZone 1 (50% - 60%):\t" + testInfo.getHrZone1Percent() + "%\n" +
                    "\t\tZone 2 (60% - 70%):\t" + testInfo.getHrZone2Percent() + "%\n" +
                    "\t\tZone 3 (70% - 80%):\t" + testInfo.getHrZone3Percent() + "%\n" +
                    "\t\tZone 4 (80% - 90%):\t" + testInfo.getHrZone4Percent() + "%\n" +
                    "\t\tZone 5 (90% - 100%):\t" + testInfo.getHrZone5Percent() + "%\n" +
                    "\t\tAbove Zone 5:\t\t" + testInfo.getHrAboveZone5Percent() + "%\n\n";
        }
        testResults = testResults + "User parameters:\n" +
                "\t\tGender: " + testInfo.getGender() + "\n" +
                "\t\tAge: " + testInfo.getAge() + "\n" +
                "\t\tHeight: " + testInfo.getHeight() + "\n" +
                "\t\tWeight: " + testInfo.getWeight() + "\n\n" +
                "Pre-test measurements:\n" +
                "\t\tDyspnea: " + testInfo.getPreTestValueDyspnea() + "\n" +
                "\t\tFatigue: " + testInfo.getPreTestValueFatigue() + "\n" +
                "\t\tBood Pressure:\n" +
                "\t\t\tSystolic: " + testInfo.getPreTestBloodPressureSystolic() + "\n" +
                "\t\t\tDiastolic: " + testInfo.getPreTestBloodPressureDiastolic() + "\n" +
                "\t\tOxygen Saturation: " + testInfo.getPreTestOxygenSaturation() + "\n\n" +
                "Post-test measurements:\n" +
                "\t\tDyspnea: " + testInfo.getPostTestValueDyspnea() + "\n" +
                "\t\tFatigue: " + testInfo.getPostTestValueFatigue() + "\n" +
                "\t\tBood Pressure:\n" +
                "\t\t\tSystolic: " + testInfo.getPostTestBloodPressureSystolic() + "\n" +
                "\t\t\tDiastolic: " + testInfo.getPostTestBloodPressureDiastolic() + "\n" +
                "\t\tOxygen Saturation: " + testInfo.getPostTestOxygenSaturation() + "\n\n";
        if (testInfo.getAdditionalComments() != null && !"".equals(testInfo.getAdditionalComments())) {
            testResults = testResults + "Additional Comments:\n" + "--------\n" + testInfo.getAdditionalComments() + "\n--------";
        }

        txtTestInfo.setMovementMethod(new ScrollingMovementMethod());
        txtTestInfo.setText(testResults);
    }
}