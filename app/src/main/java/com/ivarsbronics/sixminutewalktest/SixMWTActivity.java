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
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.format.DateFormat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public class SixMWTActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, LocationListener {
    /*logging parameter*/
    private static final String TAG = "SixMWTActivity";
    /*database instance variable - needed if database chosen is in different location than default*/
    private static final String dbInstance = "https://sixminutewalktest-ff14a-default-rtdb.europe-west1.firebasedatabase.app";
    /*time values in milliseconds for preparation phase*/
    private static final long startTimeInMillisPrep = 600000;
    /*time values in milliseconds for test phase*/
    private static final long startTimeInMillisTest = 360000;
    /*heart rate service uuid*/
    private static final UUID HEART_RATE_SERVICE = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb"); //UUID.nameUUIDFromBytes(new byte [] {(byte) 0x180D});
    /*heart rate measurement characteristic uuid*/
    private static final UUID HEART_RATE_MEASUREMENT = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb"); //UUID.nameUUIDFromBytes(new byte [] {(byte) 0x2A37});
    /*client characteristic configuration uuid - necessary for enabling notifications from ble devices*/
    private static final UUID CLIENT_CHARACTERISTIC_CONFIGURATION = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    /*arbitrary integer for FINE_LOCATION permission request*/
    private static final int FINE_LOCATION_PERMISSIONS = 100;
    /*arbitrary integer for BLUETOOTH_CONNECT permission request*/
    private static final int BLUETOOTH_CONNECT_PERMISSIONS = 101;
    /*arbitrary integer for BLUETOOTH_SCAN permissions request*/
    private static final int BLUETOOTH_SCAN_PERMISSIONS = 102;

    /*authentication variable*/
    private FirebaseAuth mAuth;
    /*current user variable*/
    private FirebaseUser currentUser;

    /*variables for interactions with layout components*/
    private Button btnBTOnOff, btnSkipHRMonitor, btnDiscoverDevices, btnStartTimer, btnResetTimer,
            btnEndTest, btnStartPreparationPhase, btnStartTestPhase, btnEndTestPrematurely,
            btnUpdateTestParameters, btnProceedToTest, btnSkipPreparation, btnContinue;
    private EditText etBloodPressureSystolic, etBloodPressureDiastolic, etOxygenSaturation, etDistance;
    private LinearLayout layoutBloodPressure;
    private TextView txtHeaderText, txtDeviceName, txtHrValue, txtTimer, txtInfo, txtTestInfo,
            txtDyspnea, txtFatigue, txtBloodPressure, txtOxygenSaturation, txtDistance;
    private Spinner spinnerDyspnea, spinnerFatigue;
    private TextInputLayout tilAdditionalComments;
    private TextInputEditText etAdditionalComment;

    /*variables for logic support*/
    private HashMap<String, String> hrMap = new HashMap();
    private HashMap<String, LatLngCustom> locationMap = new HashMap();

    private final TestInfo testInfo = new TestInfo();
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
    private boolean timerRunning = false;
    private long testStartMillis = 0;
    private long millisSinceStart = 0;
    private long timeLeftInMillis = startTimeInMillisPrep;
    private double totalDistance = 0;
    private double totalDistance1 = 0;
    private double latitude, longitude;

    private CountDownTimer countDownTimer;
    private Vibrator vibrator;
    private LocationManager locationManager;
    private Location prevLocation;
    private Criteria criteria;

    private ArrayAdapter<CharSequence> arrayAdapterScale;
    public ArrayList<BluetoothDevice> btDeviceArrayList = new ArrayList<>();
    private ListView deviceListView;

    private String valueFatigue, valueDyspnea;

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice bluetoothDevice;
    BluetoothGatt bluetoothGatt;

    public DeviceListAdapter deviceListAdapter;

    private DecimalFormat df = new DecimalFormat("#0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_six_mwt);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        btnUpdateTestParameters = findViewById(R.id.btnUpdateTestParameters);
        btnProceedToTest = findViewById(R.id.btnProceedToTest);
        btnBTOnOff = findViewById(R.id.btnBTOnOff);
        btnSkipHRMonitor = findViewById(R.id.btnSkipHRMonitor);
        btnDiscoverDevices = findViewById(R.id.btnDiscoverDevices);
        btnStartTimer = findViewById(R.id.btnStartTimer);
        btnResetTimer = findViewById(R.id.btnResetTimer);
        btnEndTest = findViewById(R.id.btnEndTest);
        btnStartPreparationPhase = findViewById(R.id.btnStartPreparationPhase);
        btnStartTestPhase = findViewById(R.id.btnStartTestPhase);
        btnEndTestPrematurely = findViewById(R.id.btnEndTestPrematurely);
        btnSkipPreparation = findViewById(R.id.btnSkipPreparation);
        btnContinue = findViewById(R.id.btnContinue);
        etBloodPressureSystolic = findViewById(R.id.etBloodPressureSystolic);
        etBloodPressureDiastolic = findViewById(R.id.etBloodPressureDiastolic);
        etOxygenSaturation = findViewById(R.id.etOxygenSaturation);
        etDistance = findViewById(R.id.etDistance);
        layoutBloodPressure = findViewById(R.id.layoutBloodPressure);
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
        txtDistance = findViewById(R.id.txtDistance);
        spinnerDyspnea = findViewById(R.id.spinnerDyspnea);
        spinnerFatigue = findViewById(R.id.spinnerFatigue);
        tilAdditionalComments = findViewById(R.id.tilAdditionalComments);
        etAdditionalComment = findViewById(R.id.etAdditionalComment);

        deviceListView = (ListView) findViewById(R.id.deviceListView);

        prepPhaseHRMin = 200;
        prepPhaseHRMax = 0;

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        /*set criteria for location accuracy*/
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);

        deviceListView.setOnItemClickListener(SixMWTActivity.this);

        txtInfo.setText("Before You start, please re-check Test Parameters and update them to have" +
                " most accurate estimated values selected for You!");
        txtInfo.setMovementMethod(new ScrollingMovementMethod());
        /*steps to setup drop down lists for dyspnea and fatigue scales*/
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
                hrMonitorSelected = false;
                testInfo.setHrMonitorSkipped("Y");
                testStartMillis = System.currentTimeMillis();
                testInfo.setTestTimeInMillis(String.valueOf(testStartMillis));
                testInfo.setTestTimeInMillisNegative(String.valueOf(-1 * testStartMillis));
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
                    testInfo.setPreTestValueDyspnea(valueDyspnea);
                    testInfo.setPreTestValueFatigue(valueFatigue);
                    testInfo.setPreTestBloodPressureSystolic(etBloodPressureSystolic.getText().toString());
                    testInfo.setPreTestBloodPressureDiastolic(etBloodPressureDiastolic.getText().toString());
                    testInfo.setPreTestOxygenSaturation(etOxygenSaturation.getText().toString());
                    prepareTestPhase();
                }
            }
        });

        btnEndTestPrematurely.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endTestPrematurely = true;
                if (countDownTimer != null) {
                    pauseTimer();
                }
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
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }
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
                }
                if (testPhase) {
                    doHrMap = false;
                    doLocationMap = false;
                    if (ActivityCompat.checkSelfPermission(SixMWTActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, BLUETOOTH_CONNECT_PERMISSIONS);
                        } else {
                            Log.d(TAG, "#### permissions not requested because of version check");
                        }
                    }
                    if (!(bluetoothGatt == null)) {
                        bluetoothGatt.disconnect();
                    }
                    locationManager.removeUpdates(SixMWTActivity.this);
                    prevLocation = null;
                    calculateAdditionalTestDetails();
                    txtDistance.setText("Distance Covered in meters (GPS)\n(If incorrect adjust value)");
                    txtDistance.setTextSize(18);
                    etDistance.setVisibility(View.VISIBLE);
                    etDistance.setText(df.format(totalDistance));
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
                            "measured by GPS is not accurate, please correct the value.\n\n" +
                            "Use 'Additional Test Comments' field to add relevant information about " +
                            "test - anything that could be useful to you or healthcare specialist!");
                    txtDyspnea.setVisibility(View.VISIBLE);
                    txtFatigue.setVisibility(View.VISIBLE);
                    txtDyspnea.setText("Post-Test Dyspnea:");
                    txtFatigue.setText("Post-Test Fatigue:");
                    txtBloodPressure.setText("Post-Test Blood Pressure:");
                    txtOxygenSaturation.setText("Post-Test Oxygen Saturation:");
                    tilAdditionalComments.setVisibility(View.VISIBLE);
                    spinnerDyspnea.setVisibility(View.VISIBLE);
                    spinnerFatigue.setVisibility(View.VISIBLE);
                    txtBloodPressure.setVisibility(View.VISIBLE);
                    txtOxygenSaturation.setVisibility(View.VISIBLE);
                    layoutBloodPressure.setVisibility(View.VISIBLE);
                    etOxygenSaturation.setVisibility(View.VISIBLE);
                    btnEndTest.setVisibility(View.VISIBLE);
                }
            }
        }.start();
        timerRunning = true;
        if (testPhase) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                txtDistance.setVisibility(View.VISIBLE);
                saveLocationData = true;
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5*1000, 0, this);
            } else {
                //if permissions not granted
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSIONS);
                }
            }
            doHrMap = true;
            doLocationMap = true;
            btnStartTimer.setVisibility(View.INVISIBLE);
        } else {
            btnStartTimer.setText("PAUSE");
        }
        btnResetTimer.setVisibility(View.INVISIBLE);
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

        }
        if (bluetoothAdapter.isEnabled()) {
            /*set android device discoverable for 60 seconds*/
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 60);
            startActivity(discoverableIntent);
        }

        btnBTOnOff.setVisibility(View.GONE);
        btnSkipHRMonitor.setVisibility(View.GONE);
        btnDiscoverDevices.setVisibility(View.VISIBLE);
    }

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

    /*method for communication with bluetooth device*/
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
                    //hrMonitorConnected = true;
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

            if (HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
                /*if device has heart rate measurement characteristic set device connected*/
                if (!hrMonitorConnected) {
                    hrMonitorConnected = true;
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
                    testInfo.setTestTimeInMillis(String.valueOf(testStartMillis));
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
                    testInfo.setPrepPhaseHRStart(String.valueOf(heartRate));
                    getPrepPhaseHRStart = false;
                }
                if (getPrepPhaseHREnd) {
                    testInfo.setPrepPhaseHREnd(String.valueOf(heartRate));
                    getPrepPhaseHREnd = false;
                }
                try {
                    txtHrValue.setText(String.format("%d", heartRate));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

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
        testInfo.setDeviceName(deviceName);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
            Log.d(TAG, "onItemClick: Pairing with " + deviceName);
            Toast.makeText(SixMWTActivity.this, "Device Chosen: " + deviceName, Toast.LENGTH_LONG).show();
            bluetoothDevice = btDeviceArrayList.get(i);

            bluetoothGatt = bluetoothDevice.connectGatt(this,true, bluetoothGattCallback);
        }
        /*if heart rate monitor is selected wait until heart rate data is received*/
        int waitCount = 0;
        while(!hrMonitorConnected && waitCount < 120) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            waitCount = waitCount + 1;
            if (waitCount == 120){
                Toast.makeText(SixMWTActivity.this, "Device: " + deviceName + " did not start transmitting heart rate data in 2 minutes.\nProceeding to next step.\n If necessary end test prematurely!", Toast.LENGTH_LONG).show();
            }
        }
        preparePreparationPhase();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        /*Log.d(TAG, "### onLocationChanged " + totalDistance);
        if (saveLocationData){
            Log.d(TAG, "### onLocationChanged: saveLocationData");
        }
        if (doLocationMap){
            Log.d(TAG, "### onLocationChanged: doLocationMap");
        }*/
        if (saveLocationData) {
            if (prevLocation == null){
                prevLocation = location;
                Log.d(TAG, "### onLocationChanged: saveLocationData: first location" + prevLocation);
            }
            else {
                if (doLocationMap) {
                    double distance = SphericalUtil.computeDistanceBetween(new LatLng(prevLocation.getLatitude(), prevLocation.getLongitude()), new LatLng(location.getLatitude(), location.getLongitude()));
                    totalDistance = totalDistance + distance;
                    //df.format(totalDistance);
                }
            }
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            if (testStartMillis != 0 && doLocationMap){
                long locationMillisSinceStart = System.currentTimeMillis() - testStartMillis;
                locationMap.put(String.valueOf(locationMillisSinceStart), new LatLngCustom(latitude, longitude));
            }

            txtDistance.setText("Distance:\n" + String.valueOf(df.format(totalDistance)));
            prevLocation = location;
        }
        else {
            locationManager.removeUpdates(this);
        }

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

    private void preparePreparationPhase() {
        btnBTOnOff.setVisibility(View.GONE);
        btnSkipHRMonitor.setVisibility(View.GONE);
        btnDiscoverDevices.setVisibility(View.GONE);
        deviceListView.setVisibility(View.GONE);
        txtHeaderText.setText("Preparation Phase");
        txtInfo.setVisibility(View.VISIBLE);
        txtInfo.setText("According to Six Minute Walk test guidelines there are absolute and relative" +
                "contraindications for the test. Consult with your healthcare specialist before doing the test.\n\n" +
                "ABSOLUTE CONTRAINDICATIONS:\n" +
                "\t1. acute myocardial infarction\n" +
                "\t2. unstable angina (acute phase)\n" +
                "\t3. uncontrolled arrhythmias causing symptoms or hemodynamic compromise\n" +
                "\t4. acute myocarditis or pericarditis\n" +
                "\t5. uncontrolled acutely decompensated HF (acute pulmonary edema)\n" +
                "\t6. acute pulmonary embolism,\n" +
                "\t7. suspected dissecting aneurysm\n" +
                "\t8. severe hypoxemia at rest\n" +
                "\t9. acute respiratory failure\n" +
                "\t10. acute non-cardiopulmonary disorder that may affect exercise performance or be aggravated by exercise (such as infection, renal failure, thyrotoxicosis)\n" +
                "\t11. mental impairment leading to inability to cooperate\n\n" +
                "RELATIVE CONTRAINDICATIONS:\n" +
                "\t1. resting heart rate >120 beats/min\n" +
                "\t2. systolic blood pressure >180 mmHg\n" +
                "\t3. diastolic pressure >100 mmHg\n\n" +
                "TEST MUST BE IMMEDIATELY STOPPED in case of chest pain, intolerable dyspnea, leg " +
                "cramps, diaphoresis or any report of not feeling well.\n\n" +
                "During preparation phase please remain seated until timer finishes countdown. " +
                "Do not perform any unnecessary movements and actions - goal of \"Preparation Phase\" is to " +
                "determine your resting heart rate.\n\n" +
                "Getting ready before \"Preparation Phase\" and \"Test Phase\":\n" +
                "\t1. Comfortable clothing should be worn.\n" +
                "\t2. Appropriate shoes for walking should be worn.\n" +
                "\t3. You should use usual walking aids during the test (cane, walker, etc.).\n" +
                "\t4. Your usual medical regimen should be continued.\n" +
                "\t5. A light meal is acceptable before early morning or early afternoon tests.\n" +
                "\t6. You should not have exercised vigorously within 2 hours of beginning the test.");
        if (hrMonitorSelected) {
            Toast.makeText(SixMWTActivity.this, "HR Monitor connected", Toast.LENGTH_LONG).show();
        }
        btnStartPreparationPhase.setVisibility(View.VISIBLE);
        btnSkipPreparation.setVisibility(View.VISIBLE);
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
        txtInfo.setVisibility(View.VISIBLE);
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
        txtInfo.setText(/*"HRStart: " + prepPhaseHRStart + "\n" +
                "HREnd: " + prepPhaseHREnd + "\n" +
                "HRMin: " + prepPhaseHRMin + "\n" +
                "HRMax: " + prepPhaseHRMax + "\n\n" +*/
                "The object of this test is to walk as far as possible for 6 minutes. Six " +
                        "minutes is a long time to walk, so you will be exerting yourself. You will " +
                        "probably get out of breath or become exhausted. You are permitted to slow " +
                        "down, to stop, and to rest as necessary. You may lean against the wall or " +
                        "any other object while rest-ing, but resume walking as soon as you are able.\n\n" +
                        "If You will be walking back and forth around the cones in the hallway: You should " +
                        "pivot briskly around the cones and continue back the other way without hesitation. " +
                        "Try turning motion by walking one lap yourself. Walk and pivot around a cone briskly.\n\n" +
                        "Application will try to track distance using GPS location of device - after test " +
                        "You will be able to adjust proposed distance if you are using more precise distance measurement.\n\n" +
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
        txtTestInfo.setVisibility(View.INVISIBLE);
        testPhase = true;
        resetTimer();
        btnBTOnOff.setVisibility(View.GONE);
        btnSkipHRMonitor.setVisibility(View.GONE);
        //txtInfo.setVisibility(View.GONE);
        txtInfo.setText("If you have granted access to GPS location tracking to this application:" +
                "\n\tfrom now on until test is finished:" +
                "\n\t\t1. don't minimize the application!" +
                "\n\t\t2. don't switch to other applications!");
        btnStartTestPhase.setVisibility(View.GONE);
        if (hrMonitorConnected) {
            txtDeviceName.setVisibility(View.VISIBLE);
            txtHrValue.setVisibility(View.VISIBLE);
        }
        txtTimer.setVisibility(View.VISIBLE);
        btnStartTimer.setVisibility(View.VISIBLE);
        btnResetTimer.setVisibility(View.INVISIBLE);
    }

    private void endTest() {
        Log.d(TAG, "### endTest()");
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
        locationManager.removeUpdates(SixMWTActivity.this);
        prevLocation = null;
        if (endTestPrematurely) {
            //Log.d(TAG, "### endTest()  endTestPrematurely = true");
            startActivity(new Intent(SixMWTActivity.this, HomeActivity.class));
            //Log.d(TAG, "### endTest()  END");
        } else {
            //Log.d(TAG, "### endTest()  endTestPrematurely = false");
            testInfo.setTestDateTime(DateFormat.format("dd-MMM-yyyy HH:mm:ss", Long.parseLong(testInfo.getTestTimeInMillis())).toString());
            testInfo.setPostTestValueDyspnea(valueDyspnea);
            testInfo.setPostTestValueFatigue(valueFatigue);
            testInfo.setPostTestBloodPressureSystolic(etBloodPressureSystolic.getText().toString());
            testInfo.setPostTestBloodPressureDiastolic(etBloodPressureDiastolic.getText().toString());
            testInfo.setPostTestOxygenSaturation(etOxygenSaturation.getText().toString());
            testInfo.setAdditionalComments(etAdditionalComment.getText().toString());
            doHrMap = false;
            doLocationMap = false;
            if (endTestPrematurely) {
                testInfo.setEndTestPrematurely("Y");
            }
            else {
                testInfo.setEndTestPrematurely("N");
            }
            testInfo.setHrMap(hrMap);
            testInfo.setLocationMap(locationMap);
            testInfo.setTotalDistance(df.format(totalDistance));
            testInfo.setUserTotalDistance(etDistance.getText().toString());
            testInfo.setPrepPhaseHRMin(String.valueOf(prepPhaseHRMin));
            testInfo.setPrepPhaseHRMax(String.valueOf(prepPhaseHRMax));

            DatabaseReference testsReference = FirebaseDatabase.getInstance(dbInstance).getReference("tests");
            testsReference.child(currentUser.getUid()).child(String.valueOf(testStartMillis)).setValue(testInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
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
            Intent intent = new Intent(SixMWTActivity.this, TestResultsActivity.class);
            intent.putExtra("EXTRA_TEST_INFO", testInfo);
            startActivity(intent);
            Log.d(TAG, "### endTest()  END");
        }
    }

    private void calculateAdditionalTestDetails() {

        Log.d(TAG, "### calculateAdditionalTestDetails START");
        final int[] age = new int[1];
        final double[] maxHR = new double[1];
        final int[] height = new int[1];
        final int[] weight = new int[1];
        final int[] testHRMin = new int[1];
        final int[] testHRMax = new int[1];
        final int[] hrBelowZone1 = new int[1];
        final int[] hrZone1 = new int[1];
        final int[] hrZone2 = new int[1];
        final int[] hrZone3 = new int[1];
        final int[] hrZone4 = new int[1];
        final int[] hrZone5 = new int[1];
        final int[] hrAboveZone5 = new int[1];
        final double[] hrBelowZone1Percent = new double[1];
        final double[] hrZone1Percent = new double[1];
        final double[] hrZone2Percent = new double[1];
        final double[] hrZone3Percent = new double[1];
        final double[] hrZone4Percent = new double[1];
        final double[] hrZone5Percent = new double[1];
        final double[] hrAboveZone5Percent = new double[1];
        final int[] averageHR = new int[1];
        final double[] estimatedDistance = new double[1];
        final String[] gender = new String[1];
        DatabaseReference databaseReference = FirebaseDatabase.getInstance(dbInstance).getReference("Users").child(currentUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Log.d(TAG, "###SNAPSHOT: " + dataSnapshot.getKey() + ": " + dataSnapshot.getValue());
                    if ("gender".equals(dataSnapshot.getKey())) {
                        if("Male".equals(dataSnapshot.getValue())){
                            gender[0] = "Male";
                        }
                        else if("Female".equals(dataSnapshot.getValue())){
                            gender[0] = "Female";
                        }
                        else{
                            //Toast.makeText(TestResultsActivity.this, "Error loading Gender value - please mark gender and re-save test parameters", Toast.LENGTH_LONG).show();
                        }
                    }
                    if("height".equals(dataSnapshot.getKey())){
                        height[0] = Integer.valueOf((String) dataSnapshot.getValue());
                    }
                    if("weight".equals(dataSnapshot.getKey())){
                        weight[0] = Integer.valueOf((String) dataSnapshot.getValue());
                    }
                    if("birthDate".equals(dataSnapshot.getKey())){
                        String bd = String.valueOf(dataSnapshot.getValue());
                        String[] dayMonthYear = bd.split(" ");
                        int d = Integer.parseInt(dayMonthYear[0]);
                        int m = getMonthNumber(dayMonthYear[1]) -1;
                        int y = Integer.parseInt(dayMonthYear[2]);

                        /*create calendar object for current day*/
                        long currentTime = System.currentTimeMillis();
                        //Log.d(TAG, "currentTime = " + currentTime);
                        Calendar calNow = Calendar.getInstance();
                        calNow.setTimeInMillis(currentTime);
                        //Log.d(TAG, "calNow = " + calNow);
                        age[0] = calNow.get(Calendar.YEAR) - y; //calBirthDate.get(Calendar.YEAR);
                        //Log.d(TAG, "age = " + age[0]);

                        /*age value postprocessing*/
                        int currMonth = calNow.get(Calendar.MONTH);// + 1;
                        //int birthMonth = calBirthDate.get(Calendar.MONTH) + 1;
                        int months = currMonth - m; //birthMonth;
                        if ((months < 0) || (months == 0 && calNow.get(Calendar.DATE) < d /*calBirthDate.get(Calendar.DATE)*/)){
                            age[0]--;
                        }
                        /*https://journals.lww.com/acsm-msse/Fulltext/2007/05000/Longitudinal_Modeling_of_the_Relationship_between.11.aspx*/
                        maxHR[0] = (double) (207 - 0.7 * age[0]);
                    }
                }

                Collection<String> hrValues = hrMap.values();
                ArrayList<String> listOfHRValues = new ArrayList<>(hrValues);
                Iterator iterator = listOfHRValues.iterator();
                int count = 0;
                int sum = 0;
                hrBelowZone1[0] = 0;
                hrZone1[0] = 0;
                hrZone2[0] = 0;
                hrZone3[0] = 0;
                hrZone4[0] = 0;
                hrZone5[0] = 0;
                hrAboveZone5[0] = 0;
                testHRMin[0] = 200;
                testHRMax[0] = 0;
                while (iterator.hasNext()) {
                    int hrValue = Integer.valueOf((String) iterator.next());
                    count = count + 1;
                    sum = sum + hrValue;
                    if (testHRMin[0] > hrValue){
                        testHRMin[0] = hrValue;
                    }
                    if (testHRMax[0] < hrValue){
                        testHRMax[0] = hrValue;
                    }
                    if (hrValue < maxHR[0] * 0.5) {
                        hrBelowZone1[0] = hrBelowZone1[0] + 1;
                    }
                    else if (hrValue >= maxHR[0] * 0.5 && hrValue < maxHR[0] * 0.6 ) {
                        hrZone1[0] = hrZone1[0] + 1;
                    }
                    else if (hrValue >= maxHR[0] * 0.6 && hrValue < maxHR[0] * 0.7 ) {
                        hrZone2[0] = hrZone2[0] + 1;
                    }
                    else if (hrValue >= maxHR[0] * 0.7 && hrValue < maxHR[0] * 0.8 ) {
                        hrZone3[0] = hrZone3[0] + 1;
                    }
                    else if (hrValue >= maxHR[0] * 0.8 && hrValue < maxHR[0] * 0.9 ) {
                        hrZone3[0] = hrZone4[0] + 1;
                    }
                    else if (hrValue >= maxHR[0] * 0.9 && hrValue <= maxHR[0]) {
                        hrZone5[0] = hrZone5[0] + 1;
                    }
                    else if (hrValue > maxHR[0]) {
                        hrAboveZone5[0] = hrAboveZone5[0] + 1;
                    }
                }

                if (count > 0) {
                    averageHR[0] = (int) (sum / count);
                    hrBelowZone1Percent[0] = (double)((int) ((double)hrBelowZone1[0] / (double)count * 10000))/100;
                    hrZone1Percent[0] = (double)((int) ((double)hrZone1[0] / (double)count * 10000))/100;
                    hrZone2Percent[0] = (double)((int) ((double)hrZone2[0] / (double)count * 10000))/100;
                    hrZone3Percent[0] = (double)((int) ((double)hrZone3[0] / (double)count * 10000))/100;
                    hrZone4Percent[0] = (double)((int) ((double)hrZone4[0] / (double)count * 10000))/100;
                    hrZone5Percent[0] = (double)((int) ((double)hrZone5[0] / (double)count * 10000))/100;
                    hrAboveZone5Percent[0] = (double)((int) ((double)hrAboveZone5[0] / (double)count * 10000))/100;
                }

                /*https://academic.oup.com/eurjcn/article/8/1/2/5929208*/
                if (gender[0] == "Female") {
                    estimatedDistance[0] = (2.11 * height[0]) - (2.29 * weight[0]) - (5.78 * age[0]) + 667;
                }
                else if (gender[0] == "Male") {
                    estimatedDistance[0] = (7.75 * height[0]) - (5.02 * age[0]) - (1.76 * weight[0]) - 309;
                }

                testInfo.setGender(gender[0]);
                testInfo.setAge(String.valueOf(age[0]));
                testInfo.setHeight(String.valueOf(height[0]));
                testInfo.setWeight(String.valueOf(weight[0]));
                testInfo.setHrBelowZone1Percent(String.valueOf(hrBelowZone1Percent[0]));
                testInfo.setHrZone1Percent(String.valueOf(hrZone1Percent[0]));
                testInfo.setHrZone2Percent(String.valueOf(hrZone2Percent[0]));
                testInfo.setHrZone3Percent(String.valueOf(hrZone3Percent[0]));
                testInfo.setHrZone4Percent(String.valueOf(hrZone4Percent[0]));
                testInfo.setHrZone5Percent(String.valueOf(hrZone5Percent[0]));
                testInfo.setHrAboveZone5Percent(String.valueOf(hrAboveZone5Percent[0]));
                testInfo.setEstimatedDistance(String.valueOf(estimatedDistance[0]));
                testInfo.setTestAverageHR(String.valueOf(averageHR[0]));
                testInfo.setHrMaxByFormula(String.valueOf(maxHR[0]));
                testInfo.setTestMinHR(String.valueOf(testHRMin[0]));
                testInfo.setTestMaxHR(String.valueOf(testHRMax[0]));
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