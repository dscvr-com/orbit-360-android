package com.dscvr.orbit360sdk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by emi on 21/08/2017.
 */

public class MotorDiscovery {
    public static final ParcelUuid SERVICE_UUID = ParcelUuid.fromString("69400001-B5A3-F393-E0A9-E50E24DCCA99");
    public static final UUID CHARACTERISTIC_UUID = UUID.fromString("69400002-B5A3-F393-E0A9-E50E24DCCA99");

    private final BluetoothAdapter adapter;
    private final Handler stopScanHandler;
    private MotorListener listener;
    private List<BluetoothDevice> knownDevices;
    private BluetoothLeScanCallback bluetoothScanCallback;
    private boolean currentlyConnecting = false;
    private Context context;
    private static final long SCAN_PERIOD = 60000;

    private class BluetoothLeScanCallback extends ScanCallback {

        private final BluetoothDeviceListener listener;

        public BluetoothLeScanCallback(BluetoothDeviceListener listener) {
            this.listener = listener;
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            Log.i(this.getClass().getSimpleName(), String.format("Device Found %s %s", device.getName(), device.getAddress()));
            listener.deviceFound(device);
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    }

    private interface BluetoothDeviceListener {
        void deviceFound(BluetoothDevice device);
    }

    public MotorDiscovery(BluetoothAdapter adapter, MotorListener listener, Context context)
    {
        this.adapter = adapter;
        this.listener = listener;
        this.knownDevices =  new ArrayList<>();
        this.stopScanHandler = new Handler();
        this.context = context;
        this.bluetoothScanCallback = new BluetoothLeScanCallback(new BluetoothDeviceListener() {
            @Override
            public void deviceFound(BluetoothDevice device) {
                addDeviceFromScan(device);
            }
        });
    }

    public void connect() {
        if (adapter == null || !adapter.isEnabled() && adapter.getBluetoothLeScanner() == null) {
            throw new MotorDiscoveryException("Bluetooth is not enabled");
        }
        List<BluetoothDevice> bluetoothDevices = searchCoupledDevices();
        if (bluetoothDevices.size() > 0) {
            connect(bluetoothDevices.get(0));
            bluetoothDevices.remove(0);
            knownDevices.addAll(bluetoothDevices);
        } else {
            findDevice();
        }
    }

    private void addDeviceFromScan(BluetoothDevice device) {
        knownDevices.add(device);
        if (!currentlyConnecting) {
            if (knownDevices.size() > 0) {
                connect(knownDevices.get(0));
                knownDevices.remove(0);
            }
        }
    }

    private void connect(BluetoothDevice device) {
        currentlyConnecting = true;
        device.connectGatt(context, true, listener);
    }

    private void findDevice() {
        stopScanHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.getBluetoothLeScanner().stopScan(bluetoothScanCallback);
            }
        }, SCAN_PERIOD);

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        ArrayList<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setServiceUuid(SERVICE_UUID).build());
        adapter.getBluetoothLeScanner().startScan(filters, settings, bluetoothScanCallback);
    }

    private List<BluetoothDevice> searchCoupledDevices() {
        Set<BluetoothDevice> bondedDevices = adapter.getBondedDevices();
        List<BluetoothDevice> contactableList = new ArrayList<>();
        if (bondedDevices.isEmpty()) {
            return new ArrayList<>();
        }
        for (BluetoothDevice device : bondedDevices) {
            if (device.getUuids() == null) {
                continue;
            }
            for (ParcelUuid uuid : device.getUuids()) {
                if (uuid.equals(SERVICE_UUID)) {
                    contactableList.add(device);
                    continue;
                }
            }

        }
        return contactableList;
    }
}
