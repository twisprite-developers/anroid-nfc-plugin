# Unity 3D Android NFC Plugin (Android Studio Project)

This repo includes an Android Studio project with all the Android native logic required by the [Unity 3D Android NFC Plugin](https://github.com/twisprite-developers/unity-nfc-plugin) project. 

## Generating the AndroidScan.jar

The Android Studio project contains a Gradle task to generate the AndroidScan.jar file inside the [/Assets/Plugins/Android](https://github.com/twisprite-developers/unity-nfc-plugin/tree/master/Assets/Plugins/Android) folder of the Unity 3D project.

To generate the .jar file open the Android Studio project, select <b>View > Tool Windows > Gradle</b> to find the Gradle tab. In the Gradle tasks tree, under <b>:AndroidNFCPlugin</b>, you will find the <b>makeJar</b> tasks. Run it by double clicking.  It will produce the <b>AndroidScan.jar</b> file under <b>/PROJECT_ROOT/AndroidNFCPlugin/build</b>.


## Code Overview

The main logic of the project is included in two classes `ScanActivity` and `ScanNFCActivity`.

The two Java classes are very similar and contain logic to initialize the Android `NfcAdapter` adapter and capture the Intents fired when a NFC tag NDEF formated is detected. Review the [Android NFC documentation](https://developer.android.com/intl/es/guide/topics/connectivity/nfc/nfc.html) for more details.

The `ScanActivy` deals directly with the Unity Player exposing the following methods: 

`void scanNFC(String gameObject, String methodName)` configures the Unity Game Object and method to be notified when a NFC Intent is handled.

`void enableBackgroundScan()` sets the background scan mode, where `ScanActivy` is the class that handles the NFC Intents.

`void disableBackgroundScan()` disables the background scan mode (disabled by default), it delegates the NFC Intents handling to the `ScanNFCActivity` that opens a new scanning view.

## License

The source code of this project is available under the [MIT](https://opensource.org/licenses/MIT) license.




