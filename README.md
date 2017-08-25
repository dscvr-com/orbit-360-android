# Orbit360 Android SDK

This is the official Android SDK for the Orbit360 Bluetooth base. 

### Hardware

The Orbit360 is a Bluetooth-Controlled base, capable of holding mobile phones and moving around a horizontal and a vertical axis. 

Through this SDK, the vertical axis (moving left or right) is reffered to as the X-axis, while the horizontal axis (moving up and down) is referred to as the Y-axis. 

### SDK Functionality

The SDK contains the following classes to develop custom applications using the Orbit360:

* `Orbit360Discovery` - Helper class which wraps the Android Bluetooth API and connects to any Orbit360 nearby. 
* `Orbit360Control` - Class to control a connected Orbit360. 
* `ScriptRunner` - Helper class to run several commands in order, asynchronously, and to estimate the current motor position. This class can be very helpful to script more complex movement patterns. 

### Usage Example

This is a minimal, illustrative usage example. For a complete usage example see the full example [here](https://github.com/dscvr.com/orbit-360-android-sdk-example).

The first step is to create a listener, which receives the connection to the Orbit360:

```
Orbit360Control orbit360;
Orbit360ConnectedListener onOrbit360Connected = new Orbit360ConnectedListener() {
    @Override
    public void orbit360Connected(Orbit360Control connectedOrbit360) {
        orbit360 = connectedOrbit360;
    }
};
```

Then, initialize the Bluetooth adapter and start looking for an Orbit360 closeby. Don't forget to handle Android's permission system correctly!

```
Orbit360Discovery discovery = new Orbit360Discovery(
	BluetoothAdapter.getDefaultAdapter(),
    new Orbit360Listener(onOrbit360Connected),
    this);

discovery.connect();
```

When an Orbit360 is found and connected, the callback above will be called. You can start controlling the Orbit360!

```
// Move the Orbit360 180 degrees counterclockwise around the X-axis!
orbit360.moveXY(new Point2f(-180, 0), new Point2f(150, 150)) 
```

### Contributors

