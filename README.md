# Android-Bluetooth
Library for simple bluetooth networking on Android (Pre-Bluetooth Low Energy)<br><br>
<h3>Some info</h3>
This is a simple java library for communication over bluetooth in android applications. This library handles finding, connecting, and talking to a client/server over bluetooth. This is an Android Studio library project.

<b>This library is built using pre-ble (bluetooth low energy) api's</b> and will run on API 15, but can run as low as 10 provided you're willing to do without a few features. Connection state is also managed for you, allowing you to tell once a server/client has disconnected from you without a fuss. 

<b>Issues such as dropped packets or temporary instabilities in connection are handled by a simple replay system</b>. If no reply is heard after x amount of time the library will automatically rebroadcast that message, as well as any others you have requested to be sent in addition at that time.

<b>Direct Read/Write to the IOStream is performed from within the library</b>. Your calling application never needs to write/read from the IOStream directly. 

<b>Messages are tagged and may only be fetched with a request containing a matching tag</b>. Instead of reading the raw messages resulting from the stream, you request messages matching a passed in tag. In this way parts of your application can send & collect their own 'baggage' without worrying about anything else. Data being sent in a game could be tagged under "move_updates" for movement related data, score updates tagged under "score_updates", health tagged under "health_updates", etc. The library handles binding data to the given tag, sending it, and then holding it on the other end until a call is made to retrieve a message with that tag.

<br><br>
<h3>Implementation Instructions</h3>
<br><br>
_It is important to note_ that this is just a quick guide for how to get started. This is not the only to implement this library, and I encourage you to adjust your implementation in order to better suit your needs.<br><br>

<b>Include this library it as a dependency</b> in your Android Studio project. A simplified explanation is as follows:<br>
  -Build a .aar from this project<br>
  -Placing it into the /libs folder of your module within your project<br>
  -Include the following line to include this library within the build.gradle 'dependencies' section, which when done would look something like this...<br>
  ```
  dependencies {
    compile 'com.uphouseworks.uphw_bluetooth:uphw-bluetooth:1.0@aar'
  }
  ```
  <br><br>
  <b>Import the library package</b> like so:<br>
  ```
  import com.uphouseworks.uphw_bluetooth_lib.*;
  ```
  <br><br>
  <b>Have your class extend BTCore.</b> BTCore itself extends Activity, and so it is intended to be used directly by an Activity extending class. _Don't worry_, extending BTCore instead of Activity won't cause any issues for you. Once done here your class declaration will look like such:
  ```
  public class DemoClass extends BTCore {
  ```
  The reason for this is the need to have access to specific callbacks only available to an Activity. Note that this will _not_ interfere with any of the callbacks in your class. This will most likely be changed in the future.
  <br><br>
  <b>Instantiate an instance of BTCore in your onCreate method</b>, and call some methods to enable bluetooth (if it's not on already), make your device discoverable, and to start a server.
  ```
  //one of a few Constructors available. This one takes a UUID to make your application uniquely indentifiable to other instances of itself, and a time to remain discoverable to other devices for.
  //Don't use this uuid, make sure to generate your own!
  btCore = new BTCore( UUID.fromString("00000000-0000-0000-0000-000000000000"), 60);
  
  //turns on bluetooth (if it's on already this won't do anything, always good to call though)
  btCore.setBTEnable(this);
  
  //Starts discovering available bluetooth devices and caching them
  btCore.setBTDiscovery();
  
  //Starts a local server that is indentifiable by the UUID given before
  btCore.startServer();
  ```
  <br><br>
  <b>Make sure to shut down bluetooth</b> when your activity is destroyed.
  ```
  @Override
    protected void onDestroy()
    {
        btCore.unregisterBroadcastReceiver();
        btCore.stopBluetooth();
        super.onDestroy();
    }
  ```
  <br><br>
  <b>Iterate through servers that have been found</b> by calling retrieveFoundDevice until null is returned.
  ```
  BluetoothDevice btDevice;
  while((btDevice = btCore.retrieveFoundDevice()) != null) {
    //do something with this bluetooth device you have found
  }
  ```
  <br><br>
  <b>Connect to a given server</b> by passing in the index of the device you wish to connect with. The indexes correspond to the order the servers are returned from 'retrieveFoundDevice()'.
  ```
  btCore.startClient(btCore.retrieveDeviceWithIndex(0));
  ```
  <br>
  <b>Check to see if we are currently attempting a connection</b> (as a client) by calling
  ```
  int isWorking = BTConnectThread.getWorkingState();
  if(isWorking == 1) {
    //attempting to connect
  } else {
    //done attempting
  }
  ```
  This will return 1 if we are currently attempting to connect, or will return otherwise when we are connected or have failed.
  <br><br>
  <b>Checking if we are currently connected</b> is done by calling 'getConnectedState'. It is important to note that this can change suddenly (i.e. a user shuts down their app suddenly) and should be checked frequently or before any major operation requiring the other user to be connected.
  ```
  boolean isConnected = BTConnectionManager.getConnectedState();
  ```
  This will return true on a successful connection, and false otherwise.
  <br><br>
  <b>Once connected write data to the stream</b> by calling 'writeData' with a message and a tag.
  ```
  BTConnectionManager.writeData("some message","TAG");
  ```
  <br><br>
  <b>Read data from the stream</b> by calling 'readData' with a tag.
  ```
  String message = BTConnectionManager.readData("TAG");
  if(message != null) {
    //do something with our message
  }
  ```
  <br><br>
  <b>Flush all messages containing a given tag</b> if you will be periodically needing to void some data. Let's say game updates once a game has already ended?
  ```
  BTConnectionManager.clearDataByTag("TAG");
  ```
  <br><br>
  <b>Reset the internal state without turning off bluetooth</b> by calling cancel on the three major modules in this library. Note if you do this you will have to call setBTDiscovery, startServer & startClient in order to find servers, restart your own, or connect to one.
  ```
  BTConnectionManager.cancel();
  BTAcceptThread.cancel();
  BTConnectThread.cancel();
  ```
  
  <br><br>
  <b>Indentifying whether or not you are the server</b> does not require any work on your part, simply make a call to 'isServer' like so (assuming you have verified you are in fact connected).
  ```
  boolean isServer = BTConnectionManager.isServer();
  
  if(isServer) {
    //do something only the server should do
  } else {
    //do something only the client should do
  }
  ```
  <br><br><br>

<h3>Background</h3>
Originally this was designed for an android application of mine, <a href="https://play.google.com/store/apps/details?id=com.uphouseworks.hangman.paid">Hangman Azul</a>. I eventually started trying to make this library as open ended as possible in order to incorporate it into future projects and to allow others to use it in their own apps as well.

<br>
<h3>Suggestions</h3>
<i>Please note</i> this project needs some work still. If you'd like to contribute and think you could really help this project blossom feel free to fork it and get back to me with a pull request. Again, there's a lot that needs to be done so I'm sure even a little bit of work would go a long way.
