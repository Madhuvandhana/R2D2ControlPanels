# R2-D2 Astromech Control Panels

A dual Android application and ESP32 firmware system designed to control interactive functions of an R2-D2 astromech dome. The project features Bluetooth Low Energy (BLE) control for holographic projectors, dynamic WS2812B LEDs (PSI status lights), dome pie panels (radar/scanner flaps), and text matrices.

## Interactive App Demonstration

Below is a walkthrough demonstration of the custom Jetpack Compose Android controller app operating the R2-D2 Dome interface:

https://github.com/Madhuvandhana/R2D2ControlPanels/assets/r2controlsapp.mp4

*(Note: If the embedded player does not load, you can view the raw file at `r2controlsapp.mp4` inside the repository).*

---

## System Architecture

The project consists of three main components:
1. **Android App (`app/`)**: A native Android app built using modern Jetpack Compose, Hilt, and Coroutines. Includes controls for:
   - ESP32 BLE peripheral device pairing and status indicators.
   - Dynamic 2D Canvas vector rendering of the R2-D2 Dome State, showing live status of the physical hardware, pie panels, logic display messages, and active projection beam.
   - Interactive Holoprojector Pan/Tilt servos via drag-and-drop joystick controller.
   - Mechanical pie panel flap triggers.
   - Rear logic display text messaging.
2. **ESP32 Firmware (`sketch_r2_d2/`)**: An Arduino sketch/firmware codebase that exposes BLE Services to receive controller instructions, translating packets into:
   - Servo motor adjustments (Pan/Tilt project angles).
   - Addressable LED color changes.
   - Solenoid/servo operations for dome panels.
   - Serial telemetry data outputs.
3. **Web Simulator (`r2d2-demo/`)**: An interactive HTML5 simulation page displaying the telemetry console, SVG vector animation feedback, and a guided tour.

---

## Protocol & BLE Commands

All commands are transmitted as standard new-line terminated UTF-8 strings:

| Command Prefix | Description | Example Payload |
| :--- | :--- | :--- |
| `HOLO:X,Y` | Sets Holoprojector Pan and Tilt servo angles (0-180) | `HOLO:90,90` |
| `LED:HEX` | Updates the main PSI LED colors | `LED:#00ff66` |
| `PANEL_TOP1_ON` / `OFF` | Toggles top radar panel flap state | `PANEL_TOP1_ON` |
| `PANEL_TOP2_ON` / `OFF` | Toggles top scanner panel flap state | `PANEL_TOP2_ON` |
| `PANEL_REAR1_ON` / `OFF` | Toggles rear saber link panel flap state | `PANEL_REAR1_ON` |
| `FLD:TEXT` | Sends string to Front Logic Display | `FLD:R2-D2 OK` |
| `RLD:TEXT` | Sends string to Rear Logic Display | `RLD:SYS-ONLINE` |
| `TEST` | Triggers a full automated diagnostics checklist sweep | `TEST` |

---

## Local Setup & Build

### Prerequisites
- Android Studio Ladybug (or higher)
- JDK 17 (Corretto or equivalent)
- Arduino IDE with ESP32 board manager installed

### Building the Android App
Set `JAVA_HOME` pointing to your local JDK installation and execute the Gradle assembler:
```bash
JAVA_HOME=/path/to/jdk-17 ./gradlew assembleDebug
```
Deploy the generated APK from `app/build/outputs/apk/debug/app-debug.apk` directly to your physical Android device.
