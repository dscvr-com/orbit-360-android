package com.dscvr.orbit360sdk;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by emi on 21/08/2017.
 */

public class MotorListener extends BluetoothGattCallback {
    public static final UUID RESPONSE_UUID = UUID.fromString("69400003-B5A3-F393-E0A9-E50E24DCCA99");
    public static final byte[] TOPBUTTON = new byte[]{(byte) 0xFE, 0x01, (byte) 0x08, (byte) 0x01, (byte) 0x08, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
    public static final byte[] BOTTOMBUTTON = new byte[]{(byte) 0xFE, 0x01, 0x08,  0x00, 0x07, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
    protected static final UUID CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private ButtonPressedListener bottomButton;
    private ButtonPressedListener topButton;
    private MotorConnectedListener listener;

    public MotorListener(MotorConnectedListener listener) {
        this(listener, null, null);
    }

    public MotorListener(MotorConnectedListener listener, ButtonPressedListener bottomButton, ButtonPressedListener topButton) {
        this.topButton = topButton;
        this.bottomButton = bottomButton;
        this.listener = listener;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        Log.i("onConnectionStateChange", "Status: " + status);
        switch (newState) {
            case BluetoothProfile.STATE_CONNECTED:
                gatt.discoverServices();
                break;
            case BluetoothProfile.STATE_DISCONNECTED:
                break;
            default: break;
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (characteristic.getUuid().equals(RESPONSE_UUID)) {
            if (Arrays.equals(characteristic.getValue(), BOTTOMBUTTON)) {
                if(bottomButton!= null) bottomButton.buttonPressed();
            }else if(Arrays.equals(characteristic.getValue(), TOPBUTTON)){
                if(bottomButton!= null) topButton.buttonPressed();
            }
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {

        List<BluetoothGattService> services = gatt.getServices();
        Log.i("Bluetooth", "onServicesDiscovered: " + services.toString());
        BluetoothGattService correctService = null;
        for (BluetoothGattService service : services) {
            if (service.getUuid().equals(MotorDiscovery.SERVICE_UUID.getUuid())) {
                correctService = service;
                break;
            }
        }
        if (correctService == null) {
            Log.d("Bluetooth", "Correct service was not found");
            return;
        }

        BluetoothGattCharacteristic characteristic = correctService.getCharacteristic(RESPONSE_UUID);
        gatt.setCharacteristicNotification(characteristic, true);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE );
        gatt.writeDescriptor(descriptor);

        listener.motorConnected(new MotorControl(gatt, correctService));
    }

    public interface ButtonPressedListener {
        void buttonPressed();
    }

    public interface MotorConnectedListener {
        void motorConnected(MotorControl control);
    }
}
