package com.ivarsbronics.sixminutewalktest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SixMWTActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, LocationListener {

    private static final String TAG = "SixMWTActivity";

    private static final String dbInstance = "https://sixminutewalktest-ff14a-default-rtdb.europe-west1.firebasedatabase.app";

    private String valueFatigue, valueDyspnea;
    private String preTestvalueDyspnea, preTestvalueFatigue, preTestBloodPressureSystolic, preTestBloodPressureDiastolic, preTestOxygenSaturation;
    private String postTestvalueDyspnea, postTestvalueFatigue, postTestBloodPressureSystolic, postTestBloodPressureDiastolic, postTestOxygenSaturation;

    private static final long startTimeInMillisPrep = 5000; //600000;
    private static final long startTimeInMillisTest = 5000; // 360000;

    /*heart rate service uuid*/
    private static final UUID HEART_RATE_SERVICE = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb"); //UUID.nameUUIDFromBytes(new byte [] {(byte) 0x180D});
    /*heart rate measurement characteristic uuid*/
    private static final UUID HEART_RATE_MEASUREMENT = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb"); //UUID.nameUUIDFromBytes(new byte [] {(byte) 0x2A37});
    /*client characteristic configuration uuid - necessary for enabling notifications from ble devices*/
    private static final UUID CLIENT_CHARACTERISTIC_CONFIGURATION = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final int FINE_LOCATION_PERMISSIONS = 100; //FINE_LOCATION_PERMISSIONS arbitrary integer for permission request
    private static final int BLUETOOTH_CONNECT_PERMISSIONS = 101; // arbitrary integer for permission request
    private static final int BLUETOOTH_SCAN_PERMISSIONS = 102; // arbitrary integer for permissions request

    private LinearLayout layoutBloodPressure;
    private FirebaseAuth mAuth;
    private Button btnBTOnOff, btnSkipHRMonitor, btnDiscoverDevices, btnStartTimer, btnResetTimer,
            btnEndTest, btnStartPreparationPhase, btnStartTestPhase, btnEndTestPrematurely,
            btnUpdateTestParameters, btnProceedToTest, btnSkipPreparation, btnContinue;
    private TextView txtHeaderText, txtDeviceName, txtHrValue, txtTimer, txtInfo, txtTestInfo, txtDyspnea, txtFatigue, txtBloodPressure, txtOxygenSaturation;
    private TextView txtDistance;
    private EditText etBloodPressureSystolic, etBloodPressureDiastolic, etOxygenSaturation, etDistance;
    private Spinner spinnerDyspnea, spinnerFatigue;
    double latitude, longitude;
    ListView deviceListView;
    public ArrayList<BluetoothDevice> btDeviceArrayList = new ArrayList<>();
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice bluetoothDevice;
    //BluetoothManager bluetoothManager;
    BluetoothGatt bluetoothGatt;
    public DeviceListAdapter deviceListAdapter;

    //FusedLocationProviderClient fusedLocationProviderClient;

    //LocationRequest locationRequest;

    private HashMap<String, String> hrMap = new HashMap();
    private HashMap<String, LatLng> locationMap = new HashMap();

    //private Map hrMap = new HashMap();
    //private Map locationMap = new HashMap();
    private int prepPhaseHRStart, prepPhaseHREnd, prepPhaseHRMin, prepPhaseHRMax;
    private boolean doHrMap = false;
    private boolean doLocationMap = false;
    private boolean preparationPhase = false;
    private boolean testPhase = false;
    private boolean getPrepPhaseHRStart = false;
    private boolean getPrepPhaseHREnd = false;
    private boolean hrMonitorSelected = false;
    private boolean hrMonitorConnected = false;
    private boolean endTestPrematurely = false;
    private boolean saveLocationData = false;
    private long testStartMillis = 0;
    private long millisSinceStart = 0;

    private FirebaseUser currentUser;

    private CountDownTimer countDownTimer;

    private boolean timerRunning;

    private long timeLeftInMillis = startTimeInMillisPrep;

    private Vibrator vibrator;

    LocationManager locationManager;

    private Location prevLocation;
    private double totalDistance = 0;
    private double totalDistance1 = 0;

    ArrayAdapter<CharSequence> arrayAdapterScale;

    //@SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_six_mwt);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        layoutBloodPressure = findViewById(R.id.layoutBloodPressure);

        btnUpdateTestParameters = findViewById(R.id.btnUpdateTestParameters);
        btnProceedToTest = findViewById(R.id.btnProceedToTest);
        btnBTOnOff = findViewById(R.id.btnBTOnOff);
        btnSkipHRMonitor = findViewById(R.id.btnSkipHRMonitor);
        btnDiscoverDevices = findViewById(R.id.btnDiscoverDevices);
        txtHeaderText = findViewById(R.id.txtHeaderText);
        txtDeviceName = findViewById(R.id.txtDeviceName);
        txtHrValue = findViewById(R.id.txtHrValue);
        txtTimer = findViewById(R.id.txtTimer);
        txtInfo = findViewById(R.id.txtInfo);
        txtTestInfo = findViewById(R.id.txtTestInfo);
        txtDyspnea = findViewById(R.id.txtDyspnea);
        txtFatigue = findViewById(R.id.txtFatigue);
        txtBloodPressure = findViewById(R.id.txtBloodPressure);
        txtOxygenSaturation = findViewById(R.id.txtOxygenSaturation);
        etBloodPressureSystolic = findViewById(R.id.etBloodPressureSystolic);
        etBloodPressureDiastolic = findViewById(R.id.etBloodPressureDiastolic);
        etOxygenSaturation = findViewById(R.id.etOxygenSaturation);
        etDistance = findViewById(R.id.etDistance);
        deviceListView = (ListView) findViewById(R.id.deviceListView);
        btnStartTimer = findViewById(R.id.btnStartTimer);
        btnResetTimer = findViewById(R.id.btnResetTimer);
        btnEndTest = findViewById(R.id.btnEndTest);
        btnStartPreparationPhase = findViewById(R.id.btnStartPreparationPhase);
        btnStartTestPhase = findViewById(R.id.btnStartTestPhase);
        btnEndTestPrematurely = findViewById(R.id.btnEndTestPrematurely);
        btnSkipPreparation = findViewById(R.id.btnSkipPreparation);
        btnContinue = findViewById(R.id.btnContinue);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        spinnerDyspnea = findViewById(R.id.spinnerDyspnea);
        spinnerFatigue = findViewById(R.id.spinnerFatigue);

        prepPhaseHRMin = 200;
        prepPhaseHRMax = 0;

        txtDistance = findViewById(R.id.txtDistance);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        deviceListView.setOnItemClickListener(SixMWTActivity.this);

        txtInfo.setText("Before You start, please re-check Test Parameters and update them to have" +
                " most accurate estimated values selected for You!");

        /*set array adapter to contain values*/
        arrayAdapterScale = ArrayAdapter.createFromResource(this, R.array.BorgScale, R.layout.spinner_layout);
        /*layout to use when list is shown to user*/
        arrayAdapterScale.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        /*attach array adapter to spinner*/
        spinnerDyspnea.setAdapter(arrayAdapterScale);
        spinnerFatigue.setAdapter(arrayAdapterScale);
        /*set spinner action on item selection*/
        spinnerDyspnea.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "#### Spinner OnItemSelected");
                valueDyspnea = spinnerDyspnea.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(TAG, "#### Spinner onNothingSelected");
                valueDyspnea = spinnerDyspnea.getSelectedItem().toString();
            }
        });
        spinnerFatigue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "#### Spinner OnItemSelected2");
                valueFatigue = spinnerFatigue.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(TAG, "#### Spinner onNothingSelected2");
                valueFatigue = spinnerFatigue.getSelectedItem().toString();
            }
        });

        btnUpdateTestParameters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SixMWTActivity.this, ProfileActivity.class));
            }
        });

        btnProceedToTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtHeaderText.setText("Connect BlueTooth Heart Rate (HR) Monitor");
                txtInfo.setVisibility(View.GONE);
                btnUpdateTestParameters.setVisibility(View.GONE);
                btnProceedToTest.setVisibility(View.GONE);
                btnBTOnOff.setVisibility(View.VISIBLE);
                btnSkipHRMonitor.setVisibility(View.VISIBLE);
            }
        });

        btnBTOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothOnOff();
            }
        });

        btnDiscoverDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discoverDevices();
            }
        });

        btnEndTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endTest();
            }
        });

        btnSkipHRMonitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startTestPhase();
                hrMonitorConnected = false;
                preparePreparationPhase();
            }
        });

        btnStartTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (timerRunning) {
                    pauseTimer();
                } else {
                    startTimer();
                }
            }
        });

        btnResetTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetTimer();
            }
        });

        btnStartPreparationPhase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPreparationPhase();
            }
        });

        btnStartTestPhase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTestPhase();
            }
        });

        btnSkipPreparation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (timerRunning) {
                    pauseTimer();
                }
                preparationPhase = true;
                btnSkipPreparation.setVisibility(View.GONE);
                btnStartPreparationPhase.setVisibility(View.GONE);
                preTestParameters();
            }
        });

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (preparationPhase) {
                    preTestvalueDyspnea = valueDyspnea;
                    preTestvalueFatigue = valueFatigue;
                    preTestBloodPressureSystolic = etBloodPressureSystolic.getText().toString();
                    preTestBloodPressureDiastolic = etBloodPressureDiastolic.getText().toString();
                    preTestOxygenSaturation = etOxygenSaturation.getText().toString();
                    prepareTestPhase();
                }
                /*if (testPhase){
                    postTestvalueDyspnea = valueDyspnea;
                    postTestvalueFatigue = valueFatigue;
                    postTestBloodPressureSystolic = etBloodPressureSystolic.getText().toString();
                    postTestBloodPressureDiastolic = etBloodPressureDiastolic.getText().toString();
                    postTestOxygenSaturation = etOxygenSaturation.getText().toString();
                }*/
            }
        });

        btnEndTestPrematurely.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endTestPrematurely = true;
                endTest();
            }
        });

        updateTimerText();
    }

    @Override
    protected void onDestroy() {
        if (ContextCompat.checkSelfPermission(SixMWTActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Log.d(TAG, "#### requesting permissions passed version check");
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, BLUETOOTH_CONNECT_PERMISSIONS);
            } else {
                Log.d(TAG, "#### permissions not requested because of version check");
            }
        } else {
            Log.d(TAG, "GRANTED ALREADY!!!! Manifest.permission.BLUETOOTH_CONNECT");
        }
        bluetoothGatt.disconnect();
        locationManager.removeUpdates(SixMWTActivity.this);
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case FINE_LOCATION_PERMISSIONS: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(SixMWTActivity.this, "FINE_LOCATION permissions granted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(SixMWTActivity.this, "To track distance outdoors automatically application requires permission to use location services.", Toast.LENGTH_LONG).show();
                }
            }
            case BLUETOOTH_CONNECT_PERMISSIONS: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(SixMWTActivity.this, "BLUETOOTH_CONNECT permissions granted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(SixMWTActivity.this, "Bluetooth connection is necessary for using Bluetooth heart rate monitor!", Toast.LENGTH_LONG).show();
                }
            }
            case BLUETOOTH_SCAN_PERMISSIONS: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(SixMWTActivity.this, "BLUETOOTH_SCAN permissions granted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(SixMWTActivity.this, "Permission to scan devices is necessary for using Bluetooth heart rate monitor!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void startPreparationPhase() {
        preparationPhase = true;
        getPrepPhaseHRStart = true;
        txtInfo.setVisibility(View.GONE);
        btnStartPreparationPhase.setVisibility(View.GONE);
        if (hrMonitorConnected) {
            txtDeviceName.setVisibility(View.VISIBLE);
            txtHrValue.setVisibility(View.VISIBLE);
        }
        txtTimer.setVisibility(View.VISIBLE);
        btnStartTimer.setVisibility(View.VISIBLE);
        btnResetTimer.setVisibility(View.INVISIBLE);
        btnSkipPreparation.setVisibility(View.VISIBLE);
    }

    private void resetTimer() {
        if (preparationPhase) {
            timeLeftInMillis = startTimeInMillisPrep;
        }
        if (testPhase) {
            timeLeftInMillis = startTimeInMillisTest;
        }
        updateTimerText();
        btnResetTimer.setVisibility(View.INVISIBLE);
        btnStartTimer.setClickable(true);
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long l) {
                timeLeftInMillis = l;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    //used from API 26
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    //deprecated from API 26
                    vibrator.vibrate(500);
                }
                timerRunning = false;
                btnStartTimer.setText("START");
                btnStartTimer.setClickable(false);
                if (preparationPhase) {
                    preTestParameters();
                    //prepareTestPhase();
                }
                if (testPhase) {
                    doHrMap = false;
                    doLocationMap = false;
                    locationManager.removeUpdates(SixMWTActivity.this);
                    txtDistance.setText("Distance Covered in meters (GPS):");
                    txtDistance.setTextSize(18);
                    etDistance.setVisibility(View.VISIBLE);
                    etDistance.setText(String.valueOf(totalDistance));
                    txtTimer.setVisibility(View.GONE);
                    btnStartTimer.setVisibility(View.GONE);
                    btnResetTimer.setVisibility(View.GONE);
                    btnEndTestPrematurely.setVisibility(View.GONE);
                    txtHrValue.setVisibility(View.GONE);
                    txtDeviceName.setVisibility(View.GONE);
                    txtHeaderText.setText("Post-Test Parameters");
                    txtInfo.setVisibility(View.VISIBLE);
                    txtInfo.setText("Please fill post-test parameters if you have tools to do the " +
                            "measurements.\nIf test was performed 1) indoors; 2) with poor GPS signal; " +
                            "3) without GPS, please enter distance covered manually.\nIf distance " +
                            "measured by GPS is not accurate, please correct the value.");
                    txtDyspnea.setVisibility(View.VISIBLE);
                    txtFatigue.setVisibility(View.VISIBLE);
                    txtDyspnea.setText("Post-Test Dyspnea:");
                    txtFatigue.setText("Post-Test Fatigue:");
                    txtBloodPressure.setText("Post-Test Blood Pressure:");
                    txtOxygenSaturation.setText("Post-Test Oxygen Saturation:");
                    spinnerDyspnea.setVisibility(View.VISIBLE);
                    spinnerFatigue.setVisibility(View.VISIBLE);
                    txtBloodPressure.setVisibility(View.VISIBLE);
                    txtOxygenSaturation.setVisibility(View.VISIBLE);
                    layoutBloodPressure.setVisibility(View.VISIBLE);
                    etOxygenSaturation.setVisibility(View.VISIBLE);
                    //btnContinue.setVisibility(View.VISIBLE);
                    btnEndTest.setVisibility(View.VISIBLE);
                }
            }
        }.start();
        timerRunning = true;
        if (testPhase) {
            doHrMap = true;
            doLocationMap = true;
            btnStartTimer.setVisibility(View.INVISIBLE);
        } else {
            btnStartTimer.setText("PAUSE");
        }
        btnResetTimer.setVisibility(View.INVISIBLE);
    }


    private void preTestParameters() {
        btnStartTimer.setVisibility(View.GONE);
        btnResetTimer.setVisibility(View.GONE);
        txtDeviceName.setVisibility(View.GONE);
        txtHrValue.setVisibility(View.GONE);
        txtTimer.setVisibility(View.GONE);
        btnSkipPreparation.setVisibility(View.GONE);
        if (prepPhaseHRMin == 200) {
            prepPhaseHRMin = 0;
        }
        getPrepPhaseHREnd = true;
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        txtHeaderText.setText("Pre-Test Parameters");
        txtInfo.setText("Please fill pre-test parameters if you have tools to do the " +
                "measurements.");
        txtDyspnea.setVisibility(View.VISIBLE);
        txtFatigue.setVisibility(View.VISIBLE);
        spinnerDyspnea.setVisibility(View.VISIBLE);
        spinnerFatigue.setVisibility(View.VISIBLE);
        txtBloodPressure.setVisibility(View.VISIBLE);
        txtOxygenSaturation.setVisibility(View.VISIBLE);
        layoutBloodPressure.setVisibility(View.VISIBLE);
        etOxygenSaturation.setVisibility(View.VISIBLE);
        btnContinue.setVisibility(View.VISIBLE);
    }

    private void prepareTestPhase() {
        txtInfo.setVisibility(View.VISIBLE);
        txtInfo.setText("HRStart: " + prepPhaseHRStart + "\n" +
                "HREnd: " + prepPhaseHREnd + "\n" +
                "HRMin: " + prepPhaseHRMin + "\n" +
                "HRMax: " + prepPhaseHRMax + "\n\n" +
                "The object of this test is to walk as far as possible for 6 minutes. Six " +
                "minutes is a long time to walk, so you will be exerting yourself. You will " +
                "probably get out of breath or become exhausted. You are permitted to slow " +
                "down, to stop, and to rest as necessary. You may lean against the wall or " +
                "any other object while rest-ing, but resume walking as soon as you are able.\n\n" +
                "You will be walking back and forth around the cones in the hallway. You should " +
                "pivot briskly around the cones and continueback the other way without hesitation. " +
                "Try turning motion by walking one lap yourself. Walk and pivot around a cone briskly.\n\n" +
                "Are you ready to do that? Please use any tool necessary for keeping lap counter if " +
                "you are doing laps. Increase count each time you turn around at this starting point. " +
                "Remember that the object is to walk AS FAR AS POSSIBLE for 6 minutes, but don’t run or jog.\n\n" +
                "Start now, or whenever you are ready by starting Timer and walking the longest distance possible.");
        preparationPhase = false;
        txtHeaderText.setText("Test Phase");
        btnStartTestPhase.setVisibility(View.VISIBLE);

        txtDyspnea.setVisibility(View.GONE);
        txtFatigue.setVisibility(View.GONE);
        spinnerDyspnea.setVisibility(View.GONE);
        spinnerFatigue.setVisibility(View.GONE);
        txtBloodPressure.setVisibility(View.GONE);
        txtOxygenSaturation.setVisibility(View.GONE);
        layoutBloodPressure.setVisibility(View.GONE);
        etOxygenSaturation.setVisibility(View.GONE);
        btnContinue.setVisibility(View.GONE);
    }

    private void updateTimerText() {
        int minutes = (int) timeLeftInMillis / 1000 / 60;
        int seconds = (int) timeLeftInMillis / 1000 % 60;
        if (seconds == 50) {
            txtTestInfo.setVisibility(View.INVISIBLE);
        }
        if (testPhase) {

            if (minutes < 6 && minutes > 0 && seconds == 0) {
                testNotification(minutes);
            }
        }
        String timeLeft = String.format("%02d:%02d", minutes, seconds);
        txtTimer.setText(timeLeft);
    }

    private void testNotification(int i) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //used from API 26
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated from API 26
            vibrator.vibrate(500);
        }
        txtTestInfo.setVisibility(View.VISIBLE);
        if (i == 5) {
            txtTestInfo.setText("You are doing well!\nYou have 5 minutes togo!");
        }
        if (i == 4) {
            txtTestInfo.setText("Keep up the good work!\nYou have 4 minutes to go!");
        }
        if (i == 3) {
            txtTestInfo.setText("You are doing well!\nYou are halfway done!");
        }
        if (i == 2) {
            txtTestInfo.setText("Keep up the good work!\nYou have only 2 minutes left!");
        }
        if (i == 1) {
            txtTestInfo.setText("You are doing well!\nYou have only 1 minute to go!");
        }
        if (i == 0) {
            txtTestInfo.setText("You did great!\nThis was good effort!");
        }
    }

    private void pauseTimer() {
        countDownTimer.cancel();
        timerRunning = false;
        btnStartTimer.setText("START");
        btnResetTimer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        /*do nothing if Back button is pressed*/
        //super.onBackPressed();
    }

    private void startTestPhase() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            txtDistance.setVisibility(View.VISIBLE);
            saveLocationData = true;
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
            Log.d(TAG, "### ACCESS_FINE_LOCATION granted!!");
        } else {
            Log.d(TAG, "### ACCESS_FINE_LOCATION NOT granted!!");
            //if permissions not granted
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.d(TAG, "### request ACCESS_FINE_LOCATION!!");
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSIONS);
            }
        }
        txtHeaderText.setText("Test Phase");
        testPhase = true;
        resetTimer();
        btnBTOnOff.setVisibility(View.GONE);
        btnSkipHRMonitor.setVisibility(View.GONE);
        txtInfo.setVisibility(View.GONE);
        btnStartTestPhase.setVisibility(View.GONE);
        if (hrMonitorSelected) {
            txtDeviceName.setVisibility(View.VISIBLE);
            txtHrValue.setVisibility(View.VISIBLE);
        }
        txtTimer.setVisibility(View.VISIBLE);
        btnStartTimer.setVisibility(View.VISIBLE);
        btnResetTimer.setVisibility(View.INVISIBLE);
    }

    //@SuppressLint("MissingPermission")
    public void bluetoothOnOff() {
        if (bluetoothAdapter == null) {
            Toast.makeText(SixMWTActivity.this, "Bluetooth adapter not available on this device", Toast.LENGTH_LONG).show();
        }
        if (ContextCompat.checkSelfPermission(SixMWTActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "MISSING!!!! Manifest.permission.BLUETOOTH_CONNECT");
            Log.d(TAG, "##### Build.VERSION.SDK_INT = " + Build.VERSION.SDK_INT);
            Log.d(TAG, "##### Build.VERSION_CODES.S = " + Build.VERSION_CODES.S);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, BLUETOOTH_CONNECT_PERMISSIONS);
            } else {
                Log.d(TAG, "#### permissions not requested because of version check");
            }
        } else {
            Log.d(TAG, "GRANTED ALREADY!!!! Manifest.permission.BLUETOOTH_CONNECT");
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            //registerReceiver(broadcastReceiver1, BTIntent);
            //Toast.makeText(SixMWTActivity.this, "Bluetooth adapter enabled!", Toast.LENGTH_LONG).show();

        }
        if (bluetoothAdapter.isEnabled()) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 60); //set discoverable for 60 sec
            startActivity(discoverableIntent);
            //Toast.makeText(SixMWTActivity.this, "Device Discoverable for 60 seconds", Toast.LENGTH_LONG).show();
        }

        btnBTOnOff.setVisibility(View.GONE);
        btnSkipHRMonitor.setVisibility(View.GONE);
        btnDiscoverDevices.setVisibility(View.VISIBLE);
    }

    private void endTest() {
        Log.d(TAG, "### endTest()");
        postTestvalueDyspnea = valueDyspnea;
        postTestvalueFatigue = valueFatigue;
        postTestBloodPressureSystolic = etBloodPressureSystolic.getText().toString();
        postTestBloodPressureDiastolic = etBloodPressureDiastolic.getText().toString();
        postTestOxygenSaturation = etOxygenSaturation.getText().toString();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, BLUETOOTH_CONNECT_PERMISSIONS);
            } else {
                Log.d(TAG, "#### permissions not requested because of version check");
            }
        }
        if (!(bluetoothGatt == null)) {
            bluetoothGatt.disconnect();
        }
        if (endTestPrematurely) {
            Log.d(TAG, "### endTest()  endTestPrematurely = true");
            startActivity(new Intent(SixMWTActivity.this, HomeActivity.class));
            Log.d(TAG, "### endTest()  END");
        } else {
            Log.d(TAG, "### endTest()  endTestPrematurely = false");
            doHrMap = false;
            doLocationMap = false;
            DatabaseReference testsReference = FirebaseDatabase.getInstance(dbInstance).getReference("tests");
            //testsReference.child(currentUser.getUid()).child(String.valueOf(String.valueOf(testStartMillis))).child("testParameters").setValue(testParameters);
            testsReference.child(currentUser.getUid()).child(String.valueOf(testStartMillis)).child("testHR").setValue(hrMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "### HR DATA SAVED!!!!!");
                    } else {
                        Log.d(TAG, task.getException().getMessage());
                        //Toast.makeText(SixMWTActivity.this, "HR Data NOT SAVED", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            testsReference.child(currentUser.getUid()).child(String.valueOf(testStartMillis)).child("testGPS").setValue(locationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        //Toast.makeText(SixMWTActivity.this, "Location Data Saved", Toast.LENGTH_LONG).show();
                        Log.d(TAG, "### LOCATION DATA SAVED!!!!!");
                    } else {
                        Log.d(TAG, task.getException().getMessage());
                        //Toast.makeText(SixMWTActivity.this, "Location Data NOT SAVED", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            //TestData testData = new TestData(hrMap, locationMap);
            Intent intent = new Intent(SixMWTActivity.this, TestResultsActivity.class);
            intent.putExtra("EXTRA_HR_MAP", hrMap);
            intent.putExtra("EXTRA_DISTANCE", totalDistance);
            intent.putExtra("EXTRA_TEST_ID", testStartMillis);
            if (endTestPrematurely) {
                intent.putExtra("EXTRA_END_PREMATURELY", "Y");
            }
            else {
                intent.putExtra("EXTRA_END_PREMATURELY", "N");
            }
            //intent.putExtra("EXTRA_GPS_MAP", locationMap);
            startActivity(intent);
            Log.d(TAG, "### endTest()  END");
        }
    }

    //@SuppressLint("MissingPermission")
    private void discoverDevices() {
        Log.d(TAG, "discoverDevices: Discovering devices");

        deviceListView.setVisibility(View.VISIBLE);

        if (bluetoothAdapter == null) {
            Toast.makeText(SixMWTActivity.this, "Bluetooth adapter not available on this device", Toast.LENGTH_LONG).show();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Toast.makeText(SixMWTActivity.this, "Bluetooth adapter is turned off!", Toast.LENGTH_LONG).show();
            } else {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "MISSING!!!! Manifest.permission.BLUETOOTH_SCAN");
                    Log.d(TAG, "##### Build.VERSION.SDK_INT = " + Build.VERSION.SDK_INT);
                    Log.d(TAG, "##### Build.VERSION_CODES.S = " + Build.VERSION_CODES.S);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN}, BLUETOOTH_SCAN_PERMISSIONS);
                    } else {
                        Log.d(TAG, "#### permissions not requested because of version check");
                    }
                    //Toast.makeText(SixMWTActivity.this, "### Manifest.permission.BLUETOOTH_CONNECT", Toast.LENGTH_LONG).show();

                    //return;
                }
                if (bluetoothAdapter.isDiscovering()) {
                    Log.d(TAG, "discoverDevices: isDiscovering");
                    bluetoothAdapter.cancelDiscovery();
                    Log.d(TAG, "discoverDevices: cancelDiscovery");

                    /*check bluetooth permissions*/
                    checkBTPermissions();

                    bluetoothAdapter.startDiscovery();
                    Log.d(TAG, "discoverDevices: startDiscovery");
                    IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(broadcastReceiver1, discoverDevicesIntent);
                }
                if (!bluetoothAdapter.isDiscovering()) {
                    Log.d(TAG, "discoverDevices: !isDiscovering");
                    /*check bluetooth permissions*/
                    checkBTPermissions();

                    bluetoothAdapter.startDiscovery();
                    Log.d(TAG, "discoverDevices: startDiscovery");
                    IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(broadcastReceiver1, discoverDevicesIntent);
                }
            }
        }
    }

    /*use BroadcastReceiver to discover bluetooth devices*/
    private BroadcastReceiver broadcastReceiver1 = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!btDeviceArrayList.contains(btDevice) && btDevice.getName() != null) {
                    btDeviceArrayList.add(btDevice);
                    Log.d(TAG, "broadcastReceiver3: onReceive: " + btDevice.getName() + "; " + btDevice.getAddress());
                    deviceListAdapter = new DeviceListAdapter(context, R.layout.bt_device_list_view, btDeviceArrayList);
                    deviceListView.setAdapter((ListAdapter) deviceListAdapter);
                }
            }
        }
    };

    /*method needed for devices running API23+; permissions in manifest is not enough - checks must be done programmatically*/
    /*checSelfPermission is compiling even if it shows issues with method call*/
    private void checkBTPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permission = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permission += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permission != 0) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            } else {
                Log.d(TAG, "checkBTPermissions: no need to check permissions");
            }
        }
    }

    BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.d(TAG, "### bluetoothGattCallback: onConnectionStateChange: gatt: " + gatt + "; status = " + status + "; newState = " + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                @SuppressLint("MissingPermission") boolean b = gatt.discoverServices();
                Log.d(TAG, "### onConnectionStateChange: boolean b " + b);
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.d(TAG, "### bluetoothGattCallback: onServicesDiscovered: gatt: " + gatt + "; status = " + status);

            BluetoothGattService hrService = gatt.getService(HEART_RATE_SERVICE);
            if (hrService != null) {
                Log.d(TAG, "### bluetoothGattCallback: onServicesDiscovered: Heart rate service found");
                BluetoothGattCharacteristic hrmCharacteristic = hrService.getCharacteristic(HEART_RATE_MEASUREMENT);
                if (hrmCharacteristic != null) {
                    Log.d(TAG, "### bluetoothGattCallback: onServicesDiscovered: HRM characteristic found ");
                    Log.d(TAG, "### bluetoothGattCallback: onServicesDiscovered: gatt: hrmCharacteristic.getProperties() = " + hrmCharacteristic.getProperties());
                    hrMonitorConnected = true;
                    if (hrmCharacteristic.getProperties() == BluetoothGattCharacteristic.PROPERTY_NOTIFY) { /*BluetoothGattCharacteristic.PROPERTY_NOTIFY*/
                        gatt.setCharacteristicNotification(hrmCharacteristic, true);
                        /*mandatory to set descriptor for notifications otherwise notifications are not sent*/
                        /*add code for disabling notification when necessary*/
                        BluetoothGattDescriptor hrmDescriptor = hrmCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIGURATION);
                        hrmDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        //hrmDescriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(hrmDescriptor);
                    } else {
                        boolean b = gatt.readCharacteristic(hrmCharacteristic);
                        Log.d(TAG, "### onServicesDiscovered: read characteristic " + b);
                    }
                }
            }
        }


        @Override
        public void onServiceChanged(@NonNull BluetoothGatt gatt) {
            super.onServiceChanged(gatt);
            Log.d(TAG, "### bluetoothGattCallback: onServiceChanged: gatt: " + gatt);
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            Log.d(TAG, "### onCharacteristicRead");

        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.d(TAG, "### onDescriptorRead: read descriptor " + descriptor.getValue());
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.d(TAG, "### onDescriptorWrite: write descriptor " + descriptor.getValue());
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            //Log.d(TAG, "### onCharacteristicChanged");
            //@SuppressLint("MissingPermission")
            //boolean b = gatt.readCharacteristic(characteristic);
            //Log.d(TAG, "### onCharacteristicChanged: read characteristic " + b);
            //if (status == BluetoothGatt.GATT_SUCCESS) {
            //Log.d(TAG, "### bluetoothGattCallback: onCharacteristicRead: status == BluetoothGatt.GATT_SUCCESS");
            if (HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
                if (!hrMonitorSelected) {
                    hrMonitorSelected = true;
                }
                //Log.d(TAG, "### bluetoothGattCallback: onCharacteristicRead: HEART_RATE_MEASUREMENT UUID = " + HEART_RATE_MEASUREMENT);
                int flag = characteristic.getProperties();
                int format = -1;
                //Log.d(TAG, "### onCharacteristicChanged: flag = " + flag);
                if ((flag & 0x01) != 0) {
                    format = BluetoothGattCharacteristic.FORMAT_UINT16;
                    //Log.d(TAG, "### onCharacteristicChanged: Heart rate format UINT16.");
                } else {
                    format = BluetoothGattCharacteristic.FORMAT_UINT8;
                    //Log.d(TAG, "### onCharacteristicChanged: Heart rate format UINT8.");
                }
                final int heartRate = characteristic.getIntValue(format, 1);
                Log.d(TAG, String.format("### onCharacteristicChanged: Received heart rate: %d", heartRate));

                if (testStartMillis == 0) {
                    testStartMillis = System.currentTimeMillis();
                    Log.d(TAG, String.valueOf(testStartMillis));
                } else {
                    millisSinceStart = System.currentTimeMillis() - testStartMillis;
                    Log.d(TAG, String.valueOf(millisSinceStart));
                }
                if (doHrMap) {
                    hrMap.put(String.valueOf(millisSinceStart), String.valueOf(heartRate));
                }
                if (preparationPhase) {
                    if (heartRate > prepPhaseHRMax) {
                        prepPhaseHRMax = heartRate;
                    }
                    if (heartRate < prepPhaseHRMin) {
                        prepPhaseHRMin = heartRate;
                    }
                }
                if (getPrepPhaseHRStart) {
                    prepPhaseHRStart = heartRate;
                    getPrepPhaseHRStart = false;
                }
                if (getPrepPhaseHREnd) {
                    prepPhaseHREnd = heartRate;
                    getPrepPhaseHREnd = false;
                }
                try {
                    txtHrValue.setText(String.format("%d", heartRate));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //}

        }

        @SuppressLint("MissingPermission")
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            Log.d(TAG, "### onCharacteristicWrite: gatt: hrmCharacteristic.getProperties() = " + characteristic.getProperties());
            boolean b = gatt.readCharacteristic(characteristic);
            Log.d(TAG, "### onCharacteristicWrite: read characteristic " + b);
        }
    };

    //@SuppressLint("MissingPermission")
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN}, BLUETOOTH_SCAN_PERMISSIONS);
            } else {
                Log.d(TAG, "#### permissions not requested because of version check");
            }
        }
        //cancel discovery - memory intensive
        bluetoothAdapter.cancelDiscovery();
        Log.d(TAG,"onItemClick: Device is selected");
        String deviceName = btDeviceArrayList.get(i).getName();
        String deviceAddress = btDeviceArrayList.get(i).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        txtDeviceName.setText(deviceName);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
            Log.d(TAG, "onItemClick: Pairing with " + deviceName);
            Toast.makeText(SixMWTActivity.this, "Device Chosen: " + deviceName, Toast.LENGTH_LONG).show();
            bluetoothDevice = btDeviceArrayList.get(i);

            bluetoothGatt = bluetoothDevice.connectGatt(this,true, bluetoothGattCallback);
        }
        preparePreparationPhase();
    }

    private void preparePreparationPhase() {
        btnBTOnOff.setVisibility(View.GONE);
        btnSkipHRMonitor.setVisibility(View.GONE);
        btnDiscoverDevices.setVisibility(View.GONE);
        deviceListView.setVisibility(View.GONE);
        txtHeaderText.setText("Preparation Phase");
        txtInfo.setVisibility(View.VISIBLE);
        txtInfo.setText("During preparation phase please remain seated until timer finishes countdown. " +
                "Do not perform any unnecessary movements and actions - goal of \"Preparation Phase\" is to " +
                "determine your resting heart rate.\n\n" +
                "Getting ready before \"Preparation Phase\" and \"Test Phase\":\n" +
                "\t1. Comfortable clothing should be worn.\n" +
                "\t2. Appropriate shoes for walking should be worn.\n" +
                "\t3. You should use usual walking aids during the test (cane, walker, etc.).\n" +
                "\t4. Your usual medical regimen should be continued.\n" +
                "\t5. A light meal is acceptable before early morning or early afternoon tests.\n" +
                "\t6. You should not have exercised vigorously within 2 hours of beginning the test.");
        if (hrMonitorConnected) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        btnStartPreparationPhase.setVisibility(View.VISIBLE);
        btnSkipPreparation.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (saveLocationData) {
            if (prevLocation == null){
                prevLocation = location;
            }
            else {
                if (doLocationMap) {
                    double distance = SphericalUtil.computeDistanceBetween(new LatLng(prevLocation.getLatitude(), prevLocation.getLongitude()), new LatLng(location.getLatitude(), location.getLongitude()));
                    float[] results = new float[3];
                    Location.distanceBetween(prevLocation.getLatitude(), prevLocation.getLongitude(), location.getLatitude(), location.getLongitude(), results);
                    totalDistance = totalDistance + distance;
                    totalDistance1 = totalDistance1 + results[0];
                }
            }
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            String text = "Latitude: " + String.valueOf(latitude) + "\n" +
                          "Longitude: " + String.valueOf(longitude) + "\n";
            if (testStartMillis != 0 && doLocationMap){
                long locationMillisSinceStart = System.currentTimeMillis() - testStartMillis;
                locationMap.put(String.valueOf(locationMillisSinceStart), new LatLng(latitude, longitude));
            }

            txtDistance.setText(String.valueOf(totalDistance) + "\n" + String.valueOf(totalDistance1));
            prevLocation = location;
        }
        else {
            locationManager.removeUpdates(this);
        }

    }

    /*@Override
    public void onLocationChanged(@NonNull List<Location> locations) {
        LocationListener.super.onLocationChanged(locations);
    }

    @Override
    public void onFlushComplete(int requestCode) {
        LocationListener.super.onFlushComplete(requestCode);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }*/
}