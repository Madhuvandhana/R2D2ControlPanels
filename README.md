# R2-D2 Astromech Control Panels

A dual Android application and ESP32 firmware system designed to control interactive functions of an R2-D2 astromech droid. The system features Bluetooth Low Energy (BLE) control for dome rotation, drive motors, body doors and utility arms, SparkFun MP3 sound effects, dynamic holographic projectors, WS2812B PSI status lights, and animated interactive modes.

---

## Interactive App Demonstration

Below is a walkthrough demonstration of the custom Jetpack Compose Android controller app operating the R2-D2 Dome interface, interactive animation modes, sound FX, and body actuators:

![Latest Android App Demo](r2d2-demo/Latest_AndroidApp.gif)

*(Note: If the GIF does not render, you can view the raw file at `r2d2-demo/Latest_AndroidApp.gif` inside the repository. The legacy dome walkthrough is also available at `r2controlsapp.gif`).*

---

## System Architecture

```
                             ┌────────────────────────┐
                             │  Android Controller    │
                             │    (Jetpack Compose)   │
                             └───────────┬────────────┘
                                         │ Bluetooth Low Energy (BLE)
                                         ▼
                             ┌────────────────────────┐
                             │   Main ESP32 Master    │
                             │    (r2_d2_v4.ino)      │
                             └────┬─────┬────┬─────┬──┘
                                  │     │    │     │
         ┌────────────────────────┘     │    │     └────────────────────────┐
         │ I2C Bus                      │    │ UART1 (GPIO 32, 38400 baud)  │ SoftwareSerial (GPIO 27, 9600)
         ▼                              │    ▼                              ▼
┌──────────────────┐                    │  ┌──────────────────────┐       ┌──────────────────────┐
│ PCA9685 Dome     │ (0x40)             │  │ SparkFun MP3 Trigger │       │ Sabertooth 2x32      │
│  - 4 Pie Panels  │                    │  │  - Sound Effects     │       │  - Left Drive Motor  │
│  - Holo Pan/Tilt │                    │  │  - Theme Music       │       │  - Right Drive Motor │
├──────────────────┤                    │  └──────────────────────┘       └──────────────────────┘
│ PCA9685 Body     │ (0x41)             │
│  - Utility Arms  │                    │ PWM / DIR (GPIO 25 & 26)
│  - Doors/Gripper │                    ▼
└──────────────────┘          ┌──────────────────────┐
                              │ MD10C Dome Driver    │
                              │  - Dome Motor Spin   │
                              └──────────────────────┘
```

---

## Key Features

### 1. Interactive Animation Modes
Pre-choreographed sequences coordinating sound, lights, dome rotation, and servo panels:
* **Happy Mode (`HAPPY_MODE`):** Joyful dome waggle, happy chitter sound, and dome panel celebration flutter.
* **Sad Mode (`SAD_MODE`):** Slow mournful dome tilt, concerned whistle sound, and subdued body posture.
* **Excited Mode (`EXCITED_MODE`):** Rapid celebratory dome spins, celebrating sounds, and active holoprojector sweep.
* **Dance Mode (`DANCE_MODE`):** Rhythmic alternating dome spins, disco track playback, and synchronized pie panel flap sequences.

### 2. Audio & Sound FX System
* **SparkFun MP3 Trigger integration** over HardwareSerial 1 at 38400 baud (`GPIO 32`).
* Over **25 emotion track categories** (Greetings, Curiosity, Warning, Scared, Celebrating, Singing, Scream, etc.).
* Dedicated Star Wars music tracks (Main Theme, Cantina, Disco, Imperial March).
* **Glitch-Free Stop Command:** Separated audio stop (`STOP_AUDIO`) from drive motor release (`STOP`). Releasing movement controls stops the motors without triggering the SparkFun toggle/restart track behavior.
* Real-time master volume slider (`VOLUME:0-100`).

### 3. Drive & Locomotion Control
* **Sabertooth 2x32 Motor Driver** over Simplified Serial (`GPIO 27`).
* Non-blocking acceleration ramping for smooth motor start/stop without battery voltage dips.
* Single-touch **Emergency Stop** and drive release halt (`STOP`).

### 4. Dome & Body Actuators
* **MD10C Dome Motor Driver** (PWM: `GPIO 25`, DIR: `GPIO 26`) with brownout suppression during simultaneous servo draws.
* **Dual PCA9685 16-channel PWM Drivers** over I2C:
  * **Dome PCA9685 (`0x40`):** 4 Top Pie Panels, Front Holoprojector Pan & Tilt.
  * **Body PCA9685 (`0x41`):** Utility Arm A, Utility Arm B, Gripper Door, Gripper Arm, Interface Door, Charge Door, Data Port Door.

---

## Master Wiring & Pinout Reference

For complete interactive wire-by-wire schematics with power distribution and slip-ring mapping, open [`r2d2-wiring.html`](r2d2-wiring.html) in any web browser.

### Master ESP32 Pin Mapping

| Peripheral | GPIO Pin | Interface | Description |
| :--- | :---: | :--- | :--- |
| **Sabertooth S1** | `27` | SoftwareSerial TX (9600 baud) | Drive motor serial packet stream |
| **SparkFun MP3 RX** | `32` | HardwareSerial 1 TX (38400 baud) | Audio playback and volume control |
| **MD10C PWM** | `25` | LEDC PWM (20 kHz, 8-bit) | Dome motor speed control |
| **MD10C DIR** | `26` | Digital Output | Dome motor rotation direction |
| **I2C SDA** | `21` | Wire (I2C) | Shared I2C data bus for PCA9685 boards |
| **I2C SCL** | `22` | Wire (I2C) | Shared I2C clock bus for PCA9685 boards |
| **AstroSerial RX** | `16` | HardwareSerial 2 RX | Astropixels controller packet stream |
| **AstroSerial TX** | `17` | HardwareSerial 2 TX | Astropixels controller packet stream |

---

## Protocol & BLE Commands

All commands are transmitted as standard new-line terminated UTF-8 strings (`\n`):

### Interactive Modes
| Command | Description |
| :--- | :--- |
| `HAPPY_MODE` | Plays happy sequence (sound, dome wiggle, panel flutter) |
| `SAD_MODE` | Plays sad sequence (concerned sound, dome dip, lights off) |
| `EXCITED_MODE` | Plays excited celebration sequence |
| `DANCE_MODE` | Plays animated dance sequence with music |

### Drive & Dome Movement
| Command | Description | Example Payload |
| :--- | :--- | :--- |
| `DRIVE:L,R` | Sets left and right track speeds (-100 to 100) | `DRIVE:60,60` |
| `STOP` | Immediately stops all drive motor motion | `STOP` |
| `DOME_LEFT` | Rotates dome counter-clockwise | `DOME_LEFT` |
| `DOME_RIGHT` | Rotates dome clockwise | `DOME_RIGHT` |
| `DOME_STOP` | Halts dome motor rotation | `DOME_STOP` |

### Audio & Sound FX
| Command | Description | Example Payload |
| :--- | :--- | :--- |
| `STOP_AUDIO` | Halts currently playing MP3 track | `STOP_AUDIO` |
| `VOLUME:pct` | Sets master volume level (0-100%) | `VOLUME:85` |
| `GREETING` | Plays random greeting chirp | `GREETING` |
| `HAPPY` / `SAD` | Plays random emotional expression | `HAPPY` |
| `SCREAM` | Plays high-pitched alarm scream | `SCREAM` |
| `STAR_WARS` | Plays Star Wars Main Title theme | `STAR_WARS` |
| `CANTINA` | Plays Cantina Band track | `CANTINA` |
| `DISCO` | Plays Star Wars Disco track | `DISCO` |
| `MARCH` | Plays Imperial March theme | `MARCH` |

### Body Doors & Utility Servos
| Command | Description |
| :--- | :--- |
| `OPEN_TOP` / `CLOSE_TOP` | Opens or closes all 4 top dome pie panels |
| `CHARGE_OPEN` / `CLOSE` | Opens or closes the charging port door |
| `DATA_OPEN` / `CLOSE` | Opens or closes the data port door |
| `INTERFACE_OPEN` / `CLOSE`| Opens or closes the computer interface door |
| `GRIPPER_DOOR_OPEN` / `CLOSE` | Opens or closes the gripper arm door |
| `GRIPPER_OPEN` / `CLOSE` | Extends or retracts the gripper claw |
| `ARM_A_DEPLOY` / `RETRACT` | Deploys or stows upper utility arm A |
| `ARM_B_DEPLOY` / `RETRACT` | Deploys or stows lower utility arm B |

---

## Jetson Companion Listener

For autonomous camera and AI processing on an onboard Nvidia Jetson, a lightweight HTTP listener is included in [`jetson_r2_listener.py`](jetson_r2_listener.py):
```bash
python3 jetson_r2_listener.py
```
Listens on port `5000` to allow the Android app to trigger on-board vision routines and software startup routines over local Wi-Fi.

---

## Local Setup & Build

### Prerequisites
* **Android Studio Ladybug (or higher)**
* **JDK 17** (Amazon Corretto, Azul Zulu, or Android Studio bundled JBR)
* **Arduino IDE 2.x** with the ESP32 board package (`esp32 by Espressif`) installed

### Building the Android App
Set `JAVA_HOME` to your local JDK installation and compile the debug APK:
```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew assembleDebug --no-daemon
```
The output APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

### Flashing the Firmware
1. Open [`sketch_r2_d2/r2_d2_v4/r2_d2_v4.ino`](sketch_r2_d2/r2_d2_v4/r2_d2_v4.ino) in the Arduino IDE.
2. Select your ESP32 board model (e.g. `ESP32 Dev Module`).
3. Ensure required libraries are installed (`Adafruit PWM Servo Driver`, `SabertoothSimplified`, `SoftwareSerial` / `EspSoftwareSerial`).
4. Click **Upload** over USB.
