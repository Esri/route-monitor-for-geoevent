# Field Worker Application for Android

Field Worker application for Android

![App](field-worker-android.png?raw=true)

## Requirements
* Android SDK 4.1.2 (including tools such as SDK Manager and AVD Manager)
* Java JDK 1.6 or later.
* ArcGIS Runtime SDK for Android 10.2 (downloadable from https://developers.arcgis.com/android/)
* Eclipse version Juno or later. 

## Building the source code:
1. Make sure all requirements are installed or present on your computer.
2. Follow instructions that can be found [here](https://developers.arcgis.com/android/install.html) to set up your IDE to work with ArcGIS Runtime SDK for Android.
3. Import the Android project.
4. The project will be built automatically.  But APK will not be created.
5. To create an unsigned APK, right-click the project in the Package Explorer and select Android Tools > Export Unsigned Application Package. Then specify the file location for the unsigned .apk.
6. To create a signed APK, right-click the project in the Package Explorer and select Android Tools > Export signed Application Package. You will be asked to either selecting an existing keystore or to create a new keystore.
7. To run the APK on an Android device, connect the device to your computer via a USB cable.  Enable USB debugging on your device.  On the Run Configuration or Debug Configuration dialog of Eclipse, select "Always prompt to pick device" option under the Target tab.  Run the project as Android Application.  Pick your device as the target.  The application will be installed and started on your device.
