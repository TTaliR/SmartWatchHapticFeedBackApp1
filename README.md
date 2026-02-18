# Smartwatch Haptic App (Wear OS)

This Wear OS application serves as the primary edge node for the Haptic Feedback System. It performs real-time physiological sensing and executes tactile feedback patterns.

## 🚀 Features
- **Heart Rate Sensing:** Continuously monitors BPM using the `BODY_SENSORS` API.
- **Bluetooth SPP Server:** Acts as a classic Bluetooth server to communicate with the Android Phone bridge.
- **Haptic Engine:** Translates complex JSON commands (pulses, intensity, duration) into physical vibrations.
- **Background Persistence:** Uses a Foreground Service to prevent system sleep during data collection.

## 🛠 Setup & Installation
1. **Device Name:** Rename your watch's Bluetooth name to `UserID-<ID>-SmartWatchID-<ID>` (e.g., `UserID-1-SmartWatchID-1`).
2. **Permissions:** Grant `Body Sensors`, `Nearby Devices`, and `Location` permissions upon launch.
3. **Deployment:** Open in Android Studio and deploy to a Wear OS device (API 30+).

## 🔗 Project Ecosystem
- [Android Phone Relay](https://github.com/TTaliR/AndroidPhoneHapticFeedBackApp) - Receives data from this app and forwards it to the cloud.
- [Haptic Backend](https://github.com/liranBecher/Smartwatch-Haptic-Workflow) - The logic engine that decides when this watch should vibrate.
- [Researcher Dashboard](https://github.com/TTaliR/ResearcherSideApp-FULL) - Used to configure the rules for this device.
