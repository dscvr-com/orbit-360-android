package com.dscvr.orbit360sdk;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

/**
 * Created by emi on 21/08/2017.
 */

public class MotorControl {
    private static final float STEPS_FOR_ONE_ROUND_X = 5111;
    private static final float STEPS_FOR_ONE_ROUND_Y = 15000;
    public static final Point2f DEGREES_TO_STEPS = new Point2f(STEPS_FOR_ONE_ROUND_X / 360f, STEPS_FOR_ONE_ROUND_Y / 360f);

    private BluetoothGatt gatt;
    private BluetoothGattService bluetoothService;

    MotorControl(BluetoothGatt gatt, BluetoothGattService service) {
        this.gatt = gatt;
        this.bluetoothService = service;
    }

    public void sendCommand(Command command) {
        BluetoothGattCharacteristic characteristic = bluetoothService.getCharacteristic(MotorDiscovery.CHARACTERISTIC_UUID);
        if(!(((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) |
                (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) > 0)) {
            throw new IllegalArgumentException("Could not write to bluetooth service.");
        }
        characteristic.setValue(command.getValue());
        gatt.writeCharacteristic(characteristic);
    }


    public void moveSteps(Point2f steps, Point2f speedInSteps) {
        sendCommand(Command.moveXYSteps(steps, speedInSteps));
    }

    public void move(Point2f degrees, Point2f speedInDegrees) {
        sendCommand(Command.moveXY(degrees, speedInDegrees));
    }

    private void stop() {
        sendCommand(Command.stop());
    }

}
