package com.ivarsbronics.sixminutewalktest;


import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.ivarsbronics.sixminutewalktest.databinding.ActivityTestResultsBinding;

import java.text.DecimalFormat;

public class TestResultsActivity extends DrawerBaseActivity {

    private static final String TAG = "TestResultsActivity";

    ActivityTestResultsBinding activityTestResultsBinding;

    private double totalDistance, estimatedDistance;

    private String testResults;

    private TextView txtTestInfo;

    private TestInfo testInfo;

    private Button btnDone;

    private boolean useUserEnteredDistance = false;

    private DecimalFormat df = new DecimalFormat("#0.00");

    @Override
    public void onBackPressed() {
        /*do nothing*/
        //super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityTestResultsBinding = ActivityTestResultsBinding.inflate(getLayoutInflater());
        setContentView(activityTestResultsBinding.getRoot());

        txtTestInfo = findViewById(R.id.txtTestInfo);
        btnDone = findViewById(R.id.btnBack);

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TestResultsActivity.this, DashboardActivity.class));
            }
        });

        Intent intent = getIntent();
        testInfo = (TestInfo)intent.getParcelableExtra("EXTRA_TEST_INFO");

        if (!testInfo.getTotalDistance().equals(testInfo.getUserTotalDistance())) {
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
        if (testInfo.getEstimatedDistance() != null && !"".equals(testInfo.getEstimatedDistance())) {
            estimatedDistance = Double.parseDouble(testInfo.getEstimatedDistance().toString().replace(",","."));
        }

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
                (double)((int)(totalDistance/estimatedDistance*10000))/100 + "% of estimated distance.\n\n" +
                "Your estimated maximum Heart Rate according to formula \"(207 - 0.7 * Age)\"" +
                "is:\n\t\t" + testInfo.getHrMaxByFormula() + "\n";
        if (testInfo.getPrepPhaseHRMin() != null && !"".equals(testInfo.getPrepPhaseHRMin())) {
            if (Integer.parseInt(testInfo.getPrepPhaseHRMin()) > 0) {
                testResults = testResults + "Minimal registered heart rate during preparation phase was:\n\t\t" + testInfo.getPrepPhaseHRMin() + "\n";
            }
        }
        if ("".equals(testInfo.getHrMonitorSkipped()) || testInfo.getHrMonitorSkipped() == null) {
            testResults = testResults + "Your average Heart Rate during test was:\n\t\t" + testInfo.getTestAverageHR() + "\n\n" +
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