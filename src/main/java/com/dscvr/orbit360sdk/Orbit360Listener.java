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
 * This class wraps and calls callbacks when an Orbit360 is connected or
 * a remote button is pressed.
 */
public class Orbit360Listener extends BluetoothGattCallback {
    /**
     * UUID for the characteristic of the remote control.
     */
    public static final UUID RESPONSE_UUID = UUID.fromString("69400003-B5A3-F393-E0A9-E50E24DCCA99");
    /**
     * Characteristic value for the upper remote button.
     */
    public static final byte[] TOPBUTTON = new byte[]{(byte) 0xFE, 0x01, (byte) 0x08, (byte) 0x01, (byte) 0x08, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
    /**
     * Characteristic value for the lower remote button.
     */
    public static final byte[] BOTTOMBUTTON = new byte[]{(byte) 0xFE, 0x01, 0x08,  0x00, 0x07, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
    /**
     * UUID for the characteristic which notifies completed movements. This is not used at the moment.
     */
    protected static final UUID CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private ButtonPressedListener bottomButton;
    private ButtonPressedListener topButton;
    private Orbit360ConnectedListener listener;

    /**
     * Creates a new instance of this class.
     * @param listener The Orbit360 connected listener, which will be called as soon as an Orbit360 connects.
     */
    public Orbit360Listener(Orbit360ConnectedListener listener) {
        this(listener, null, null);
    }

    /**
     * Creates a new instance of this class.
     * @param listener The Orbit360 connected listener, which will be called as soon as an Orbit360 connects.
     * @param bottomButton The ButtonPressedListener which will be called as soon as the lower button on the remote is pressed. Can be null.
     * @param topButton The ButtonPressedListener which will be called as soon as the top button on the remote is pressed. Can be null.
     */
    public Orbit360Listener(Orbit360ConnectedListener listener, ButtonPressedListener bottomButton, ButtonPressedListener topButton) {
        this.topButton = topButton;
        this.bottomButton = bottomButton;
        this.listener = listener;
    }

    /**
     * Do not call.
     */
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

    /**
     * Do not call.
     */
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

    /**
     * Do not call.
     */
    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {

        List<BluetoothGattService> services = gatt.getServices();
        Log.i("Bluetooth", "onServicesDiscovered: " + services.toString());
        BluetoothGattService correctService = null;
        for (BluetoothGattService service : services) {
            if (service.getUuid().equals(Orbit360Discovery.SERVICE_UUID.getUuid())) {
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
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);

        listener.orbit360Connected(new Orbit360Control(gatt, correctService));
    }

    /**
     * Interface for ButtonPresses.
     */
    public interface ButtonPressedListener {
        void buttonPressed();
    }

    /**
     * Interface for a successful connection to an Orbit360.
     */
    public interface Orbit360ConnectedListener {
        void orbit360Connected(Orbit360Control control);
    }
}
