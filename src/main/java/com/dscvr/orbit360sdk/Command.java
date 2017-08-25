package com.dscvr.orbit360sdk;

import java.nio.ByteBuffer;

/**
 * This class represents a single command, which can be sent to the Orbit360.
 */
public class Command {
    private static final byte[] EMPTY = new byte[0];
    private byte[] value = new byte[32];

    private Point2f steps;
    private Point2f speed;

    private Command(Point2f steps, Point2f speed) {
        this.steps = steps;
        this.speed = speed;
    }

    /**
     * @return The speed, in steps per second, for this command.
     */
    public Point2f getSpeed() {
        return speed;
    }

    /**
     * @return The steps to move for thi scommand.
     */
    public Point2f getSteps() {
        return steps;
    }

    /**
     * Creates a new command from horizontal (X) and vertical (Y) movement, in degrees.
     * @param degrees The distance to move, in degrees.
     * @param speedInDegrees The movement speed, in degrees per second.
     * @return The command, which can be sent to the Orbit360.
     */
    public static Command moveXY(Point2f degrees, Point2f speedInDegrees) {
        return moveXYSteps(degrees.mul(Orbit360Control.DEGREES_TO_STEPS), speedInDegrees.mul(Orbit360Control.DEGREES_TO_STEPS));
    }

    /**
     * Creates a new command from horizontal (X) and vertical (Y) movement, in steps.
     * @param steps The distance to move, in steps.
     * @param speed The movement speed, in steps per second.
     * @return The command, which can be sent to the Orbit360.
     */
    public static Command moveXYSteps(Point2f steps, Point2f speed) {
        Command command = new Command(steps, speed);
        byte[] dataX = command.createDataWithoutFullStep((int) steps.getX(), (int) speed.getX());
        byte[] dataY = command.createData((int) -steps.getY(), (int) speed.getY());
        byte[] data = command.mergeArrays(dataX, dataY);
        //func: 0x03 -> x + y motor
        command.createCommand((byte) 0x03, data);
        return command;
    }

    /**
     * Creates a stop-command, which, if sent, will cause the Orbit360 to cease movement.
     * @return The command, which can be sent to the Orbit360.
     */
    public static Command stop() {
        Command command = new Command(new Point2f(0, 0), new Point2f(0, 0));
        command.createCommand((byte) 0x04, EMPTY);
        return command;
    }

    private void createCommand(byte function, byte[] data) {
        value[0] = (byte) 0xFE;
        value[1] = (byte) data.length;
        value[2] = function;
        for (int i = 0; i < data.length; i++) {
            value[i + 3] = data[i];
        }
        int checksum = 0;
        for (int i = 0; i < data.length + 3; i++) {
            checksum += value[i];
        }
        value[data.length + 3] = (byte) (checksum & 0xFF);
    }

    private byte[] mergeArrays(byte[] dataX, byte[] dataY) {
        byte[] result = new byte[dataX.length + dataY.length];
        for (int i = 0; i < dataX.length; i++) {
            result[i] = dataX[i];
        }
        for (int i = dataX.length; i < result.length; i++) {
            result[i] = dataY[i - dataX.length];
        }
        return result;
    }


    private byte[] createData(int steps, int speed) {
        byte[] data = createDataWithoutFullStep(steps, speed);
        byte[] newData = new byte[data.length + 1];
        for (int i = 0; i < data.length; i++) {
            newData[i] = data[i];
        }
        //full stepps
        newData[data.length] = (byte) 0x00;
        return newData;
    }

    private byte[] createDataWithoutFullStep(int steps, int speed) {
        byte[] stepsAsArray = getByteArray(steps);
        byte[] data = new byte[stepsAsArray.length + 2];
        for (int i = 0; i < stepsAsArray.length; i++) {
            data[i] = stepsAsArray[i];
        }
        //add Speed
        byte[] speedInArray = getByteArray(speed);
        data[stepsAsArray.length] = speedInArray[2];
        data[stepsAsArray.length + 1] = speedInArray[3];
        return data;
    }

    private byte[] getByteArray(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    /**
     * @return The command's byte representation.
     */
    public byte[] getValue() {
        return value;
    }
}
