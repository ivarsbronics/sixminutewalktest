package com.ivarsbronics.sixminutewalktest;

import androidx.annotation.NonNull;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

import java.lang.reflect.Field;
import java.util.Calendar;

public class ProfileActivity extends DrawerBaseActivity {

    private static final String TAG = "RegisterActivity";

    private static final String dbInstance = "https://sixminutewalktest-ff14a-default-rtdb.europe-west1.firebasedatabase.app";

    ActivityProfileBinding activityProfileBinding;

    private FirebaseAuth mAuth;
    private EditText etxtHeight, etxtWeight;
    private Button btnDatePicker, btnSaveTestParameters;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private DatePickerDialog datePickerDialog;

    private String genderChoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityProfileBinding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(activityProfileBinding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        
        initDatePicker(-1, -1, -1);
        
        btnDatePicker = findViewById(R.id.btnDatePicker);
        btnDatePicker.setText(getTodaysDate());
        //txtGender = findViewById(R.id.txtGender);
        radioGroup = findViewById(R.id.rgGender);
        etxtHeight = findViewById(R.id.etxtHeight);
        etxtWeight = findViewById(R.id.etxtWeight);
        btnSaveTestParameters = findViewById(R.id.btnSaveTestParameters);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance(dbInstance).getReference("Users").child(currentUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Log.d(TAG, "###SNAPSHOT: " + dataSnapshot.getKey() + ": " + dataSnapshot.getValue());
                    if ("gender".equals(dataSnapshot.getKey())) {
                        if("Male".equals(dataSnapshot.getValue())){
                            //RadioButton rb = findViewById(R.id.rbMale);
                            //rb.setChecked(true);
                            radioGroup.check(R.id.rbMale);
                            genderChoice = "Male";
                        }
                        else if("Female".equals(dataSnapshot.getValue())){
                            //RadioButton rb = findViewById(R.id.rbFemale);
                            //rb.setChecked(true);
                            radioGroup.check(R.id.rbFemale);
                            genderChoice = "Female";
                        }
                        else{
                            Toast.makeText(ProfileActivity.this, "Error loading Gender value - please mark gender and re-save test parameters", Toast.LENGTH_LONG).show();
                        }
                    }
                    if("height".equals(dataSnapshot.getKey())){
                        etxtHeight.setText("" + dataSnapshot.getValue());
                    }
                    if("weight".equals(dataSnapshot.getKey())){
                        etxtWeight.setText("" + dataSnapshot.getValue());
                    }
                    if("birthDate".equals(dataSnapshot.getKey())){
                        btnDatePicker.setText("" + dataSnapshot.getValue());
                        String bd = String.valueOf(dataSnapshot.getValue());
                        String[] dayMonthYear = bd.split(" ");
                        Log.d(TAG, dayMonthYear[0]);
                        Log.d(TAG, dayMonthYear[1]);
                        Log.d(TAG, dayMonthYear[2]);
                        int d = Integer.parseInt(dayMonthYear[0]);
                        int m = getMonthNumber(dayMonthYear[1]) -1;
                        int y = Integer.parseInt(dayMonthYear[2]);
                        Log.d(TAG, "" + d);
                        Log.d(TAG, "" + m);
                        Log.d(TAG, "" + y);
                        initDatePicker(d,m,y);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        /*FirebaseDatabase.getInstance(dbInstance).getReference("Users").child(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, String.valueOf(task.getResult().getValue()));
                    DataSnapshot userInfo = task.getResult();
                    Log.d(TAG, String.valueOf(userInfo));
                }
                else{
                    Log.d(TAG, "Error reading data: " + task.getException());
                }
            }
        });*/
        
        btnDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDatePicker();
            }
        });

        btnSaveTestParameters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int radioButtonId = radioGroup.getCheckedRadioButtonId();
                if (radioButtonId == -1){
                    //txtGender.setError("Gender must be selected");
                    //txtGender.requestFocus();
                    AlertDialog.Builder alertDialog  = new AlertDialog.Builder(ProfileActivity.this);
                    alertDialog.setTitle(" ");
                    //alertDialog.setIcon(R.drawable.wrong);
                    alertDialog.setMessage("Please select gender that fits you the most!\nOtherwise test feedback will not be accurate!");
                    alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int which) {
                        }
                    });
                    alertDialog.show();
                }
                else {
                    saveTestParameters();
                }
            }
        });
    }

    private void saveTestParameters() {
        String birthDate = String.valueOf(btnDatePicker.getText());
        String height = String.valueOf(etxtHeight.getText());
        String weight = String.valueOf(etxtWeight.getText());
        String gender = genderChoice; //String.valueOf(radioGroup.getCheckedRadioButtonId());
        UserInfo userInfo = new UserInfo(birthDate, height, gender, weight);

        DatabaseReference usersReference = FirebaseDatabase.getInstance(dbInstance).getReference("Users");

        //remove: used for class field verification
        for (Field field : userInfo.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            String name = field.getName();
            Object value = null;
            try {
                value = field.get(userInfo);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            System.out.printf("%s: %s%n", name, value);
        }
        FirebaseUser currentUser = mAuth.getCurrentUser();
        usersReference.child(currentUser.getUid()).setValue(userInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Parameters Saved Successful!", Toast.LENGTH_SHORT).show();

                    //Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
                    // makes sure that getting to previous activity is not possible, i.e., back button does not return to RegisterActivity
                    // makes sure that only one HomeActivity instance is available - reusing existing instance or creating new
                    //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    //startActivity(intent);
                }
                else{
                    Log.d(TAG, task.getException().getMessage());
                }
            }
        });
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.rbMale:
                if (checked) {
                    genderChoice = "Male";
                    break;
                }
            case R.id.rbFemale:
                if (checked) {
                    genderChoice = "Female";
                    break;
                }
        }
    }


    private String getTodaysDate() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        month = month + 1;
        return day + " " + getMonthText(month) + " " + year;
    }

    private void initDatePicker(int d, int m, int y) {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                String birthdate = day + " " + getMonthText(month) + " " + year;
                btnDatePicker.setText(birthdate);
            }
        };

        int year, day, month;
        Calendar calendar = Calendar.getInstance();
        if (d == -1 && m == -1 && y == -1) {
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
            Log.d(TAG, "" + day);
            Log.d(TAG, "" + month);
            Log.d(TAG, "" + year);
        }
        else {
            year = y;
            month = m;
            day = d;
        }

        datePickerDialog = new DatePickerDialog(this, AlertDialog.THEME_HOLO_LIGHT, dateSetListener, year, month,day);
    }

    private String getMonthText(int month) {
        if (month == 1){
            return "JAN";
        }
        if (month == 2){
            return "FEB";
        }
        if (month == 3){
            return "MAR";
        }
        if (month == 4){
            return "APR";
        }
        if (month == 5){
            return "MAY";
        }
        if (month == 6){
            return "JUN";
        }
        if (month == 7){
            return "JUL";
        }
        if (month == 8){
            return "AUG";
        }
        if (month == 9){
            return "SEP";
        }
        if (month == 10){
            return "OCT";
        }
        if (month == 11){
            return "NOV";
        }
        if (month == 12){
            return "DEC";
        }

        //in case of unexpected text return text error
        return "ERROR";
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

    private void openDatePicker() {
        datePickerDialog.show();
    }


}