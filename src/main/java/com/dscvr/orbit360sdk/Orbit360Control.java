package com.dscvr.orbit360sdk;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

/**
 * This class provides a direct interface to send commands to the Orbit360.
 */
public class Orbit360Control {
    /**
     * Count of steps in one full horizontal rotation (around the X-axis)
     */
    public static final float STEPS_FOR_ONE_ROUND_X = 5111;
    /**
     * Count of steps in one full vertical rotation (around the Y-axis)
     */
    public static final float STEPS_FOR_ONE_ROUND_Y = 15000;
    /**
     * Conversion factors from degrees to steps, for both axis.
     */
    public static final Point2f DEGREES_TO_STEPS = new Point2f(STEPS_FOR_ONE_ROUND_X / 360f, STEPS_FOR_ONE_ROUND_Y / 360f);

    private BluetoothGatt gatt;
    private BluetoothGattService bluetoothService;

    Orbit360Control(BluetoothGatt gatt, BluetoothGattService service) {
        this.gatt = gatt;
        this.bluetoothService = service;
    }

    /**
     * Sends a command to the Orbit360. If a previous command did not finish execution, the previous
     * command will be interrupted.
     * @param command The command to send.
     */
    public void sendCommand(Command command) {
        BluetoothGattCharacteristic characteristic = bluetoothService.getCharacteristic(Orbit360Discovery.CHARACTERISTIC_UUID);
        if(!(((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) |
                (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) > 0)) {
            throw new IllegalArgumentException("Could not write to bluetooth service.");
        }
        characteristic.setValue(command.getValue());
        gatt.writeCharacteristic(characteristic);
    }

    /**
     * Shorthand to move the Orbit360 by a certain number of steps.
     * Please refer Command.moveXYSteps for information about the parameters.
     */
    public void moveSteps(Point2f steps, Point2f speedInSteps) {
        sendCommand(Command.moveXYSteps(steps, speedInSteps));
    }

    /**
     * Shorthand to move the Orbit360 by a certain number of degrees.
     * Please refer Command.move for information about the parameters.
     */
    public void move(Point2f degrees, Point2f speedInDegrees) {
        sendCommand(Command.moveXY(degrees, speedInDegrees));
    }

    /**
     * Shorthand to stop the Orbit360.
     * Please refer Command.stops for information about the method.
     */
    private void stop() {
        sendCommand(Command.stop());
    }

}
