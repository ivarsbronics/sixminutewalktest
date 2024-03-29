package com.ivarsbronics.sixminutewalktest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class TestListAdapter extends ArrayAdapter<TestInfo> {

    private DecimalFormat df = new DecimalFormat("#0.00");

    public TestListAdapter(@NonNull Context context, ArrayList<TestInfo> testArrayList) {
        super(context, R.layout.test_list_item, testArrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        TestInfo testInfo = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.test_list_item, parent, false);
        }

        TextView txtTestDateTime = convertView.findViewById(R.id.txtTestDateTime);
        TextView txtTestDistance = convertView.findViewById(R.id.txtTestDistance);
        TextView txtTestAverageHR = convertView.findViewById(R.id.txtTestAverageHR);

        txtTestDateTime.setText(testInfo.getTestDateTime());
        if (testInfo.getUserTotalDistance() != "-") {
            txtTestDistance.setText("Distance: " + df.format(Double.valueOf(testInfo.getUserTotalDistance().toString().replace(",","."))) + " m");
        }
        else {
            txtTestDistance.setText("Distance: " + df.format(Double.valueOf(testInfo.getTotalDistance().toString().replace(",","."))) + " m");
        }
        txtTestAverageHR.setText("HR (average): " + testInfo.getTestAverageHR() + " pbm"); //(testInfo.getAverageHR());

        return convertView; //super.getView(position, convertView, parent);
    }
}
