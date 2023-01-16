package com.ivarsbronics.sixminutewalktest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HomeActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    private static final String TAG = "HomeActivity";

    private static final String dbInstance = "https://sixminutewalktest-ff14a-default-rtdb.europe-west1.firebasedatabase.app";

    /*heart rate service uuid*/
    private static final UUID HEART_RATE_SERVICE = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb"); //UUID.nameUUIDFromBytes(new byte [] {(byte) 0x180D});
    /*heart rate measurement characteristic uuid*/
    private static final UUID HEART_RATE_MEASUREMENT = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb"); //UUID.nameUUIDFromBytes(new byte [] {(byte) 0x2A37});
    /*client characteristic configuration uuid - necessary for enabling notifications from ble devices*/
    private static final UUID CLIENT_CHARACTERISTIC_CONFIGURATION = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private FirebaseAuth mAuth;
    private Button btnLogOut, btnDiscoverDevices, btnEndTest;
    private TextView txtDeviceName, txtHrValue;
    ListView deviceListView;
    public ArrayList<BluetoothDevice> btDeviceArrayList = new ArrayList<>();
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice bluetoothDevice;
    public DeviceListAdapter deviceListAdapter;

    private Map hrMap = new HashMap();
    private boolean doHrMap = true;
    private long testStartMillis = 0;
    private long millisSinceStart = 0;

    private FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        btnLogOut = findViewById(R.id.btnLogOut);
        btnDiscoverDevices = findViewById(R.id.btnDiscoverDevices);
        txtDeviceName = findViewById(R.id.txtDeviceName);
        txtHrValue = findViewById(R.id.txtHrValue);
        deviceListView = (ListView) findViewById(R.id.deviceListView);
        btnEndTest = findViewById(R.id.btnEndTest);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        deviceListView.setOnItemClickListener(HomeActivity.this);

        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
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
    }

    private void endTest() {
        doHrMap = false;
        DatabaseReference testsReference = FirebaseDatabase.getInstance(dbInstance).getReference("tests");
        testsReference.child(currentUser.getUid()).child(String.valueOf(String.valueOf(testStartMillis))).setValue(hrMap);
    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
    }

    @SuppressLint("MissingPermission")
    private void discoverDevices() {
        Log.d(TAG, "discoverDevices: Discovering devices");

        ////if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ////return;
        ////}

        if (bluetoothAdapter == null) {
            Toast.makeText(HomeActivity.this, "Bluetooth adapter not available on this device", Toast.LENGTH_LONG).show();
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);
            Toast.makeText(HomeActivity.this, "Bluetooth adapter enabled!", Toast.LENGTH_LONG).show();

        }

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300); //set discoverable for 300 sec
        startActivity(discoverableIntent);

        /*if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
            //Toast.makeText(HomeActivity.this, "Bluetooth adapter disabled!", Toast.LENGTH_LONG).show();
        }*/
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
        if(!bluetoothAdapter.isDiscovering()){
            Log.d(TAG, "discoverDevices: !isDiscovering");
            /*check bluetooth permissions*/
            checkBTPermissions();

            bluetoothAdapter.startDiscovery();
            Log.d(TAG, "discoverDevices: startDiscovery");
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(broadcastReceiver1, discoverDevicesIntent);
        }
    }

    /*use BroadcastReceiver to discover bluetooth devices*/
    private BroadcastReceiver broadcastReceiver1 = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!btDeviceArrayList.contains(btDevice) && btDevice.getName() != null){
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
    private void checkBTPermissions(){
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permission = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permission += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permission != 0){
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            }
            else{
                Log.d(TAG, "checkBTPermissions: no need to check permissions");
            }
        }
    }

    BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.d(TAG, "### bluetoothGattCallback: onConnectionStateChange: gatt: " + gatt + "; status = " + status + "; newState = " + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED){
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
            if(hrService != null){
                Log.d(TAG, "### bluetoothGattCallback: onServicesDiscovered: Heart rate service found");
                BluetoothGattCharacteristic hrmCharacteristic = hrService.getCharacteristic(HEART_RATE_MEASUREMENT);
                if (hrmCharacteristic != null){
                    Log.d(TAG, "### bluetoothGattCallback: onServicesDiscovered: HRM characteristic found ");
                    Log.d(TAG, "### bluetoothGattCallback: onServicesDiscovered: gatt: hrmCharacteristic.getProperties() = " + hrmCharacteristic.getProperties());
                    if (hrmCharacteristic.getProperties() == 16){
                        gatt.setCharacteristicNotification(hrmCharacteristic, true);
                        /*mandatory to set descriptor for notifications otherwise notifications are not sent*/
                        /*add code for disabling notification when necessary*/
                        BluetoothGattDescriptor hrmDescriptor = hrmCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIGURATION);
                        hrmDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        //hrmDescriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(hrmDescriptor);
                        /*print descriptor list if necessary*/
                        /*for (BluetoothGattDescriptor descriptor : hrmCharacteristic.getDescriptors()) {
                            Log.d(TAG, "### bluetoothGattCallback: onServicesDiscovered: gatt: descriptor " + descriptor.getUuid() + "; " + descriptor.getPermissions() + "; " + descriptor.getValue());
                            boolean b = gatt.readDescriptor(descriptor);
                            Log.d(TAG, "### onServicesDiscovered: read descriptor " + b);
                        }*/

                        //Log.d(TAG, "### bluetoothGattCallback: onServicesDiscovered: gatt: hrmCharacteristic.getProperties() = " + hrmCharacteristic.getProperties());
                    }
                    else {
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
            @SuppressLint("MissingPermission") boolean b = gatt.readCharacteristic(characteristic);
            Log.d(TAG, "### onCharacteristicChanged: read characteristic " + b);
            //if (status == BluetoothGatt.GATT_SUCCESS) {
            //Log.d(TAG, "### bluetoothGattCallback: onCharacteristicRead: status == BluetoothGatt.GATT_SUCCESS");
            if (HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
                //Log.d(TAG, "### bluetoothGattCallback: onCharacteristicRead: HEART_RATE_MEASUREMENT UUID = " + HEART_RATE_MEASUREMENT);
                int flag = characteristic.getProperties();
                int format = -1;
                Log.d(TAG, "### onCharacteristicChanged: flag = " + flag);
                if ((flag & 0x01) != 0) {
                    format = BluetoothGattCharacteristic.FORMAT_UINT16;
                    Log.d(TAG, "### onCharacteristicChanged: Heart rate format UINT16.");
                } else {
                    format = BluetoothGattCharacteristic.FORMAT_UINT8;
                    Log.d(TAG, "### onCharacteristicChanged: Heart rate format UINT8.");
                }
                final int heartRate = characteristic.getIntValue(format, 1);
                Log.d(TAG, String.format("### onCharacteristicChanged: Received heart rate: %d", heartRate));

                if (testStartMillis == 0){
                    testStartMillis = System.currentTimeMillis();
                    Log.d(TAG, String.valueOf(testStartMillis));
                }
                else{
                    millisSinceStart = System.currentTimeMillis() - testStartMillis;
                    Log.d(TAG, String.valueOf(millisSinceStart));
                }
                if (doHrMap) {
                    hrMap.put(String.valueOf(millisSinceStart), String.valueOf(heartRate));
                }
                try{
                    txtHrValue.setText(String.format("%d",heartRate));
                }
                catch (Exception e){
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

    @SuppressLint("MissingPermission")
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
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
            Toast.makeText(HomeActivity.this, "Device Chosen: " + deviceName, Toast.LENGTH_LONG).show();
            bluetoothDevice = btDeviceArrayList.get(i);

            BluetoothGatt bluetoothGatt = bluetoothDevice.connectGatt(this,true, bluetoothGattCallback);
        }
    }
}