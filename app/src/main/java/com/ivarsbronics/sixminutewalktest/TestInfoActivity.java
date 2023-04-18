package com.ivarsbronics.sixminutewalktest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class TestInfoActivity extends AppCompatActivity {

    private TestInfo testInfo;
    private TextView txtTestInfo;
    private double totalDistance, estimatedDistance;
    private boolean useUserEnteredDistance;
    private String testResults;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_info);

        txtTestInfo = findViewById(R.id.txtTestInfo);

        Intent intent = getIntent();
        testInfo = (TestInfo)intent.getParcelableExtra("EXTRA_TEST_INFO");

        if (!testInfo.getTotalDistance().equals(testInfo.getUserTotalDistance())) {
            if (testInfo.getTotalDistance() != null && !"".equals(testInfo.getTotalDistance())) {
                totalDistance = Double.parseDouble(testInfo.getTotalDistance());
            }
        }
        else {
            if (testInfo.getUserTotalDistance() != null && !"".equals(testInfo.getUserTotalDistance())) {
                totalDistance = Double.parseDouble(testInfo.getUserTotalDistance());
            }
            useUserEnteredDistance = true;
        }
        if (testInfo.getEstimatedDistance() != null && !"".equals(testInfo.getEstimatedDistance())) {
            estimatedDistance = Double.parseDouble(testInfo.getEstimatedDistance());
        }

        testResults = testInfo.getTestDateTime() + "\n\nAccording to provided test parameters estimated distance for healthy" +
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
                "\t\tOxygen Saturation: " + testInfo.getPostTestOxygenSaturation() + "\n";

        txtTestInfo.setMovementMethod(new ScrollingMovementMethod());
        txtTestInfo.setText(testResults);
    }
}