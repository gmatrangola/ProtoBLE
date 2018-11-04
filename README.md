# ProtoBLE BETA
Protobuf messages and asynchronyous RPC over BLE between Mobile Devices
running Android or iOS and Raspberry Pi (or similar Linux OS)

## Description
This is a BLE Messaging system implemented using 
[Google Protcol Buffers (a.k.a. Protobuf)](https://developers.google.com/protocol-buffers/).
This implementation uses the Android Bluetooth BLE API as on the Central
or iOS CoreBluetooth (client) side and the Linux BlueZ API on the
Peripheral (server) side.

Use of ProtoBLE abstracts a lot of the details for BLE protocol to make
it easer to define custom services and enforces a well
documnted cross-platform message definition using Protobufs. It also
effectively increases the 20 byte transfer limit to 65534 bytes. Note
that there are some practial limits on the message sizes and care
should be taken to ensure that the messages don't exceed 65534 bytes or
are too big for the low data bandwidth.

This repo provides libraries, Protobuf language extensions and a
protoc plugins to generate code for each of the target platforms.

## Early Release!
This is an early release of the code. Knida pre-beta. If you are brave,
go for it. Just remember the licensing limitatins and that we don't
warranty or imply sutibility for any purpose. This library is currently
used for a project that is still under development and not in the field.
ProtoBLE will move out of beta after that project gets fielded or
there is a significant purchase of the commercial licnese.

## Setup

### Linux (Peripheral) Server Side Dependencies:

Here is what you need to develop a BLE Peripheral on Linux.

#### Overview Requirements for Raspberry Pi Raspbian (may work on other Debian Distros)

1. My fork of ble-java (see above). Install dependencies mentioned in it's README.md file.
2. Oracle JDK for Java 8 (or better)
3. BlueZ 5.43 or better
4. libunixsocket-java (apt-get install libsocket-java)
5. d-bus Java library libdbus-java

#### Detailed Installation Steps

1. Install Protoc

```
sudo apt-get install protobuf-compiler
```

If protoc is not available for your platform, you can download and install it from. https://github.com/google/protobuf/releases/latest

2. Install my fork of ble-java into your local Maven Repository
```
git clone https://github.com/gmatrangola/ble-java
cd ble-java
./gradlew install
```

3. Clone and cd into the repo

```
git clone git@github.com:gmatrangola/ProtoBLE.git
cd ProtoBLE
```

4. Install the **ProtoBLE Code Generator** local repo and binary
```
./gradlew :CodeGen:install :CodeGen:distTar
mkdir $HOME/tools
tar -xvf CodeGen/build/distributions/CodeGen-1.0-SNAPSHOT.tar -C $HOME/tools/
```

5. Install the ProtoBLE Linux Lib on the Linux box where you develop the Peripheral code.
```
./gradlew :LibProtoBLELinux:install

```

6. Make sure you have BlueZ set up for BLE Advertising: See: https://github.com/gmatrangola/ble-java#bluez-compatibility

Note: You may be able to configure your Bluetooth device permissions so that the app doesn't have to be run as root. If you figure that out, please let me know. :)

### Android BLE Central

This will only work on a machine with the latest Android Devlepoment Kit installed. You can
skip the first three steps if you are doing your Andorid development on the same Linux box
where you are developing the server/Peripheral

1. Install the Protoc Protobuf compiler for your platform. The installation will be slightly
diffrent for Linux, Windows and MacOS.

2. Clone and cd into the repo
```
git clone git@github.com:gmatrangola/ProtoBLE.git
cd ProtoBLE
```

3. Install the **ProtoBLE Code Generator** local repo and binary (if you haven't already)
```
./gradlew :CodeGen:install :CodeGen:distTar
mkdir $HOME/tools
tar -xvf CodeGen/build/distributions/CodeGen-1.0-SNAPSHOT.tar -C $HOME/tools/
```

2. Install LibProtoBLEAndroid into the local Maven Repository on the system where you run Android Studio
```
./gradlew :LibProtoBLEAndroid:install
```

### iOS BLE Central
The iOS code for ProtoBLE libaries and examples are in an XCode project under the ProtoBLEiOS directory.
Details for dependencies and building the project are in a README.md contained within that directory. This is just a high-level
overview.

1. Install Swift Protobuf. See: https://github.com/apple/swift-protobuf
1. Load the ProtoBLEiOS project into XCode
1. Build
1. Export the ProtoBleiOS library through cocopods (TBD)

## Usage

Create a messaging API for sending and receiving data using Proto3, make UUIDs for the
 service, and each message. For the Peripheral, create a Plain Old Java project with an instance of 
 ```ProtoBleServer```, ```MessageInputBleCharacteristic```, and ```MessageOutputBleCharacteristic```.
 Then create a central on Android by creating a class that extends ```ProtoBleCentralService```
  
Take a look at *ExampleApiHello*, *ExampleAndroidCentral*, *ProtoBLEiOS*, and *ExampleLinuxServer*.

For Java builds, take a look at the protobuf.plugins section of the build.gradle file in
ExampleAndroidCentral and ExampleLinuxServer.

For Swift builds, take a look at ProtoBLEiOS/Example/ProtoBLEiOS/GenerateProto.sh

For your projects, you'll replace the ```path``` property with the approprte full path to where the
codegen scripts were installed. 
 
### api.proto:

It might be convenient to create a simple Java Library Module for the API that includes the proto 
along with some interfaces containing constants for the UUIDs for use in both the Peripheral and 
Central code. See ExampeApiHello. Use the com.google.protobuf to make this a seamless part of the
build process 
 

```proto
message Introduction {
    string name = 1;
    string salutation = 2;
}

message Greeting {
    int64 timesttamp = 1;
    string greeting = 2;
}
```

**see:** ExampleApiHello

### BLE peripheral (Server-side)

The Peripheral code is intended to run on Linux with the Oracle Java 8 JDK and BlueZ 5.46 installed.
  A Raspberry Pi 3 or W running Raspbian Stretch should do fine. Create or enhance a Java 8 project 
      with lib-proto-ble-linux, and the api library you cretaed above by adding them to the 
      dependencies section of your Gradle file.
        
1. Install my fork of java-ble and LibProtoBleLinux (see above), and make sure you have mavenLocal in the
     '''repositories''' section of your server's build.gradle file. see setup above.
     
2. Add the '''dependencies''' to your build.gradle file. 

```groovy
dependencies {
    implementation group: electrazoom name: 'LibPotoBLELinux', version: '1.0'
    implementation project(':your-java-lib-with-proto-file')
}

```

3. Create a Java Class with the ProtoBleServer, Input and Output Characteristics.

```java
protoServer = new ProtoBleServer(APP_PATH, SERVICE_GUID);
introductionMessageCharacteristic = new MessageInputBleCharacteristic("intro",
        INTRO_INPUT_CHARACTERISTIC_GUID);
greetingMessageCharacteristic = new MessageOutputBleCharacteristic("hello",
        GREETING_OUTPUT_CHARACTERISTIC_GUID);

introductionMessageCharacteristic.setInputListener(new MessageInputBleCharacteristic.InputListener() {
    @Override
    public void onServiceConnected() {
        LOG.info("ServiceConnected");
    }

    @Override
    public void onError(String error) {
        LOG.error("Error on input characteristic " + error);
    }

    @Override
    public void onMessageInput(byte[] message) {
        LOG.debug("onMessageInput " + message.length);
        try {
            Hello.Introduction introduction = Hello.Introduction.parseFrom(message);
            Hello.Greeting.Builder greeting = Hello.Greeting.newBuilder();
            greeting.setTimesttamp(System.currentTimeMillis());
            greeting.setGreeting("hey, " + introduction.getSalutation() + ", " +
                    introduction.getName());
            greetingMessageCharacteristic.sendMessage(greeting.build().toByteArray());
        } catch (InvalidProtocolBufferException e) {
            LOG.error("Error converting message", e);
        }
    }
});
protoServer.addMessageCharacteristic(greetingMessageCharacteristic);
protoServer.addMessageCharacteristic(introductionMessageCharacteristic);
```

**see:** example-linux-server

### iOS BLE Central (Client Side)

1. In your own porject, improt ProtoBleiOS library
1. Create a schell script to call protoc with the correct parameters (an iOS build process expert can probably come up with a better way)
```bash
#!/bin/sh

protoc --proto_path=../../../CodeGen/src/main/proto/ --proto_path=../../../ExampleApiHello/src/main/proto --plugin=protoc-gen-protoble=../../../codegen/build/install/CodeGen/bin/SwiftClientPlugin --swift_out=Generated --protoble_out=Generated hello.proto
```
1. Run the script and import the resulting swift files into your project.
1. The example uses one View Controller to select the BLE Peripheral and
aonther to intract with ProtoBLE. There are other ways to this this.
But the main point is to connect the ProtoBleCentralManager to the device
that advertises the desired service
```swift
    var centralManager: ProtoBLECentralManager!
    var helloBleService: HelloWorldBleService!

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        centralManager = ProtoBLECentralManager.shared
        helloBleService = HelloWorldBleService(delegate: self)
    }
//...
    var device : DeviceInformation? {
        didSet {
            centralManager.connectedDevice = device
        }
    }
```
1. Implement the Delegate genrated for your service to handle asynchronyous callbacks.
```swift
class HelloViewController: UIViewController, HelloWorldDelegate {
//...
    func helloWorldDidComplete(_ greeting: Greeting) {
        print ("onHelloWorld \(greeting.greeting)")
        responseLabel.text = greeting.greeting
        responseLabel.isEnabled = true
    }

    func helloWorldMessageProgress(current: Int, total: Int) {
        print("helloWorldProgress \(current) / \(total)")
    }

    func getTimeDidComplete(_ timeResponse: TimeResponse) {
        print ("onGetTime \(timeResponse.formattedTime)")
    }

    func getTimeMessageProgress(current: Int, total: Int) {
        print("getTimeMessageProgress \(current) / \(total)")
    }

    func wasConnected() {
        print("ProtoBleService Connected")
        writeButton.isEnabled = true
    }

    func didError(_ error: String) {
        print("Protoble Error: \(error)")
    }

    func onBleConnected() {
        print("HelloBleService: onBleConnected")
        writeButton.isEnabled = true
    }

    func onNoMessageAvailable() {
        print("HelloBleService: onNoMessageAvailable")
    }
}
```
10. Call the service methods using the genrated class.
```swift
 @IBAction func onWriteButton(_ sender: Any) {
        let intro = Introduction.with {
            $0.name = nameText.text!
            $0.salutation = salutationText!.text!
        }
        do {
            try helloBleService.helloWorld(introduction: intro)
        }
        catch {
            print ("Error!: \(error)")
        }
    }
```
**see:** ProtoBleiOS

### Android BLE Central (Client Side)

1. Install the dependencies described above.
2. Create an Android Mobile application module in Android Studio. Choose compleSdkVersion 27 (or better), minSdkVersion 21, and targetSdkVersion 27 (or better)
3. Add the protobuf plugin to the buildscript section see https://github.com/google/protobuf-gradle-plugin
```groovy
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'com.google.protobuf:protobuf-gradle-plugin:0.8.6'
    }
}
```
4. Add the protobuf section with the protoble plugins
```groovy
apply plugin: 'com.google.protobuf'

protobuf {
    protoc {
        // This doesn't work on Raspberry Pi because there is no protoc compiled for arm in mavencentral.
        artifact = 'com.google.protobuf:protoc:3.5.1-1'
        // may need to specify path if protoc isn't in the usual place.
        // path = '/usr/wherever'
    }
    plugins {
        protoble {
            path = "${System.properties['user.home']}/Tools/CodeGen-1.0/bin/AndroidClientGenerator"
        }
    }

    generateProtoTasks {
        all().each { task ->
            task.plugins {
                protoble {}
            }
            task.builtins {
                remove java // common java classes already built in the :example-api-hello
            }
        }
    }
}
```
5. Add the electrazoom library and your protobuf API and the LibProtoBLEAndroid library to ```dependeincies``` in the
build.gradle file.

```groovy
dependencies {
...
    implementation 'com.electrazoom:LibProtoBLEAndroid:0.1'

    implementation 'com.electrazoom.network:youAPI:1.0-SNAPSHOT' // this be diffrent for you
    // or
    implementation project(':your-java-lib-with-proto-file')
...
}

```
6. Let Android Studio do it's dance syncing the IDE to the changes in build.gradle
1. Run the build with assembleDebug (or approprate gradle target for your project).
1. The Build process will create an Android Service Java class and put under your build directory.
1. Add the new service to the `AndroidManifset.xml`
1. Add the `uses-permissions` and `uses-feature sections` for BLE to the `AndroidManifest.xml`

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.matrangola.protoble.mobile">

    <!-- ble perelectrazoom   <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

    <!-- Tell android to make sure the device has BLE -->
    <uses-feature
        android:name="android.hardware.bluetooth_le"
        android:required="true" />

    <application>
        <!-- stuff deleted for brevity -->
        <service android:name=".HelloBleCentralService" />
    </application>
</manifest>
```
11. Create an Activity to find and initiate the connection to the BLE service. The Example uses two different activities for this. ```SelectPeripheralActivity.kt``` finds BLE Peripherals advertising the SERVICE_GUID defined the the api module. When the user selects the Peripheral it starts another Activity to connect to the Android Service which, in turn, connects to the Peripheral.
1. Make sure your application has ```ACCESS_COARSE_LOCATION``` runtime permission before doing the scan. The example uses com.github.babedev.dexter.dsl for this.
1. Use the Android ```BluetoothAdapter``` API to create a ```BluetoothLeScanner```.

```kotlin
    // Find instance of example custom peripheral service from https://github.com/tongo/ble-java
    private val filters = listOf(
            ScanFilter.Builder().setServiceUuid(helloServiceUuid).build(),
            ScanFilter.Builder().setServiceUuid(echoServiceUuid).build()
            )
    private fun findAdapterWithPermission() {
        runtimePermission {
            permission(Manifest.permission.ACCESS_COARSE_LOCATION) {
                granted { findAdapter() }
                denied { Log.e(TAG, "Permission denied") }
            }
        }
    }

    private fun findAdapter(): Boolean {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter
        if (adapter?.isEnabled == false) {
            Log.d(TAG, "Adapter not enabled.")
            startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT)
            return false
        }
        startScan(adapter)
        return true
    }

```
14. Bind to your Local Service that extends BleCentralService using the standard Service Pattern (see: https://developer.android.com/guide/components/bound-services.html)
1. Pass a ```BluetoothDevice``` returned by the scan to ```serivce.connect()``` along with the UUIDs.
```kotlin
    private fun connectToBleGatt() {
        val result = intent.getParcelableExtra<ScanResult>("scanResult")
        Log.d(TAG, "Selected Scan result = " + result)
        if (null != result) {
            val device = result.device
            clientService?.connect(device, UUID.fromString(HelloUtil.SERVICE_GUID),
                    UUID.fromString(HelloUtil.INTRO_INPUT_CHARACTERISTIC_GUID),
                    UUID.fromString(HelloUtil.GREETING_OUTPUT_CHARACTERISTIC_GUID))
        }
    }
```
16. Implement a ```ProtoBleCentralService.ResponseListener``` to handle messages coming from the Peripheral and errors. Note that messages will come back in a worker thread, need to marshal them the the UI thread if you want to display them.
```kotlin
    inner class ServiceResponseListener : ProtoBleCentralService.ResponseListener {
        override fun onServiceConnected() {
            runOnUiThread {sendButton.isEnabled = true}
        }

        override fun onResponse(bytes: ByteArray) {
            Log.d(TAG, "onResponse $bytes")
            val message = Hello.Greeting.parseFrom(bytes)
            runOnUiThread {responseText.text = "msg: ${message?.timesttamp} ${message?.greeting}"}
        }


        override fun onError(error: String?) {
            Log.e(TAG, "onError $error")
        }
    }
```
17. Send Messages to the Peripheral using the ```service.writeMessage()``` method. Use the Protobuf Builder and toByteArray() features.
```kotlin
    private fun sayHello() {
        Log.d(TAG, "sayHello")
        val builder = Hello.Introduction.newBuilder()
        builder.name = salutationText.text.toString()
        builder.salutation = nameText.text.toString()
        val message = builder.build()
        clientService!!.writeMessage(message.toByteArray())
    }
```
**see:** ExampleAndroidClient

## How to Compile and Install examples
Buid with included Gradle wrapper and/or this project can be Opened in Android Studio.

### Andorid Central Code

1. Android Development System (Android Studio)
2. Android Phone Lollipop (API 21) or later

### Command line build

First, follow the steps outlined for cloning the repo and setting up
the code generator and libs.

Build and install to connected Android device

`./gradlew installDebug`
 
or
 
`./gradlew installRelease`
 

### Sample Linux Parpherial

Build the Java applicaiton package

`./gradlew :ExampleLinuxServer:installDist`

Run

`./ExampleLinuxSrver/build/install/ExampleLinuxSrver/bin/ExampleLinuxServer`

If you get a `Exception in thread "main" java.lang.UnsatisfiedLinkError: no unix-java in java.library.path` error when you run, you might need to add this to your $HOME/.bashrc file and relogin...
```
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/lib/jni
```

## Weird CM

The settings.gradle file in the root of the repo is set up to run on
macOS or Desktop Linux (like Ubuntu) or Embedded Linux (Like Raspian)
It assumes that you don't have the dependencies for the Peripheral stuff
if you are running on macOS (becuase that requires Network Manager etc)
and it checks to see if you have the Android SDK (or are running in
Android Studio) for builds on embedded devices where Android Development
is not supported.

## References

These other projects were used to get ideas and a better understanding of BLE on Android and Linux.

* Android + BLE + Protobuf: https://github.com/4ntoine/protobuf-ble-rpc
* Linux Java BLE Peripheral:  https://github.com/tongo/ble-java
* Linux BlueZ BLE + entertaining documentation: https://github.com/nettlep/gobbledegook

See:
* http://code.google.com/p/protobuf/
* http://code.google.com/apis/protocolbuffers/docs/overview.html
* https://grpc.io/
* https://github.com/sdeo/protobuf-socket-rpc

# License

This Project has an easy to use Dual License setup. Feel free to use
the GPL License here for prototyping, internal demos, and derivative
GPL Projects. However, there is a very inexpensive Commercial License
avaialble at http://electrazoom.com that you can use in your own money
making schemes.