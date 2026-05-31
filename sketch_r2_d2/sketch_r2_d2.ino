#include <Wire.h>
#include <Adafruit_PWMServoDriver.h>
#include "BluetoothSerial.h"

// ==================================================
// BLUETOOTH
// ==================================================

BluetoothSerial SerialBT;

// ==================================================
// ASTROPIXELS SERIAL2
// ==================================================

HardwareSerial AstroSerial(2);

// ==================================================
// PCA9685
// ==================================================

Adafruit_PWMServoDriver pwm =
    Adafruit_PWMServoDriver();

// ==================================================
// SERVO RANGE
// ==================================================

#define SERVOMIN 200
#define SERVOMAX 500

// ==================================================
// HOLO SERVOS
// ==================================================

#define HOLO_PAN      0
#define HOLO_TILT     1

// ==================================================
// TOP PIE PANELS
// ==================================================

#define PANEL_1       12
#define PANEL_2       13
#define PANEL_3       14
#define PANEL_4       15

// ==================================================
// PANEL POSITIONS
// ==================================================

#define PANEL_CLOSED  180
#define PANEL_OPEN    25

// ==================================================
// STATES
// ==================================================

bool panelsOpen = false;

bool holoLightsEnabled = true;
bool rearholoLightsEnabled = true;

// ==================================================
// COMMAND BUFFER
// ==================================================

String incoming = "";

// ==================================================
// SERVO FUNCTION
// ==================================================

void setServo(
    int channel,
    int angle
) {

  angle = constrain(
      angle,
      0,
      180
  );

  int pulse = map(
      angle,
      0,
      180,
      SERVOMIN,
      SERVOMAX
  );

  pwm.setPWM(
      channel,
      0,
      pulse
  );
}

// ==================================================
// ASTROPIXELS COMMAND
// ==================================================

void sendAstroCommand(
    String cmd
) {

  AstroSerial.print("@AP");

  AstroSerial.print(cmd);

  AstroSerial.print('\r');

  Serial.print(
      "ASTRO CMD: "
  );

  Serial.println(cmd);
}

// ==================================================
// RAW COMMAND
// ==================================================

void sendRawCommand(
    String cmd
) {

  AstroSerial.print(cmd);

  AstroSerial.print('\r');

  Serial.print(
      "RAW CMD: "
  );

  Serial.println(cmd);
}

// ==================================================
// REAR HOLO DIRECTION EFFECT
// ==================================================

unsigned long lastEffect = 0;

void holoMoveEffect(
    int pan,
    int tilt
) {

  if (!holoLightsEnabled) {
    return;
  }
    if(!rearholoLightsEnabled){
    return;
  }

  if (
      millis() - lastEffect < 250
  ) {
    return;
  }

  lastEffect = millis();

  // ==========================================
  // CENTER
  // ==========================================

  if (

      pan > 70 &&
      pan < 110 &&

      tilt > 70 &&
      tilt < 110

  ) {

    sendRawCommand(
        "*HP102"
    );
  }

  // ==========================================
  // UP
  // ==========================================

  else if (

      tilt < 60 &&

      pan > 70 &&
      pan < 110

  ) {

    sendRawCommand(
        "*HP202"
    );
  }

  // ==========================================
  // LEFT
  // ==========================================

  else if (

      pan < 60 &&

      tilt > 70 &&
      tilt < 110

  ) {

    sendRawCommand(
        "*HP302"
    );
  }

  // ==========================================
  // UPPER LEFT
  // ==========================================

  else if (

      pan < 60 &&
      tilt < 60

  ) {

    sendRawCommand(
        "*HP402"
    );
  }

  // ==========================================
  // LOWER LEFT
  // ==========================================

  else if (

      pan < 60 &&
      tilt > 120

  ) {

    sendRawCommand(
        "*HP502"
    );
  }

  // ==========================================
  // RIGHT
  // ==========================================

  else if (

      pan > 120 &&

      tilt > 70 &&
      tilt < 110

  ) {

    sendRawCommand(
        "*HP602"
    );
  }

  // ==========================================
  // UPPER RIGHT
  // ==========================================

  else if (

      pan > 120 &&
      tilt < 60

  ) {

    sendRawCommand(
        "*HP702"
    );
  }

  // ==========================================
  // LOWER RIGHT
  // ==========================================

  else if (

      pan > 120 &&
      tilt > 120

  ) {

    sendRawCommand(
        "*HP802"
    );
  }

  // ==========================================
  // PULSE EFFECT
  // ==========================================

  sendRawCommand(
      "*HPS302"
  );
}

// ==================================================
// HOLO LIGHTS
// ==================================================

void holoLightsOn() {

  holoLightsEnabled = true;

  sendRawCommand("*ON01");

  delay(100);

  sendRawCommand("*ON02");

  delay(100);

  sendRawCommand("*ON03");

  Serial.println(
      "ALL HOLOS ON"
  );
}

void holoLightsOff() {

  holoLightsEnabled = false;

  sendRawCommand("*OF01");

  delay(100);

  sendRawCommand("*OF02");

  delay(100);

  sendRawCommand("*OF03");

  Serial.println(
      "ALL HOLOS OFF"
  );
}

// ==================================================
// MODES
// ==================================================

void leiaMode() {

  sendAstroCommand(
      "LE30000"
  );
}

void rainbowMode() {

  sendAstroCommand(
      "LE100500"
  );
}

// ==================================================
// TWITCH
// ==================================================

void enableTwitch() {

  sendRawCommand(
      "HPA199"
  );
}

void disableTwitch() {

  sendRawCommand(
      "HPA198"
  );
}

// ==================================================
// CENTER HOLOS
// ==================================================

// void centerHolos() {

//   sendRawCommand(
//       "*ST00"
//   );

//   delay(300);

//   sendRawCommand(
//       "*HP101"
//   );

//   delay(300);

//   sendRawCommand(
//       "*HP102"
//   );

//   delay(300);

//   sendRawCommand(
//       "*HP103"
//   );
// }

// ==================================================
// REAR LOGIC DISPLAY
// ==================================================

void rearLogicMessage(
    String message
) {

  sendRawCommand(
      "@3M" + message
  );
}

// ==================================================
// HOLO CONTROL
// ==================================================

void moveHolo(
    int pan,
    int tilt
) {

  setServo(
      HOLO_PAN,
      pan
  );

  setServo(
      HOLO_TILT,
      tilt
  );

   holoMoveEffect(
      pan,
      tilt
  );

  Serial.print(
      "PAN: "
  );

  Serial.print(pan);

  Serial.print(
      " | TILT: "
  );

  Serial.println(tilt);
}

// ==================================================
// PANEL HELPER
// ==================================================

void setPanels(
    int angle
) {

  setServo(
      PANEL_1,
      180 - angle
  );

  setServo(
      PANEL_2,
      180 - angle
  );

  setServo(
      PANEL_3,
      180 - angle
  );

  setServo(
      PANEL_4,
      180 - angle
  );
}

// ==================================================
// OPEN PANELS
// ==================================================

void openTop() {

  if (panelsOpen) {
    return;
  }

  for (

      int angle = PANEL_CLOSED;

      angle >= PANEL_OPEN;

      angle -= 2

  ) {

    setPanels(angle);

    delay(15);
  }

  panelsOpen = true;

  Serial.println(
      "PANELS OPEN"
  );
}

// ==================================================
// CLOSE PANELS
// ==================================================

void closeTop() {

  if (!panelsOpen) {
    return;
  }

  for (

      int angle = PANEL_OPEN;

      angle <= PANEL_CLOSED;

      angle += 2

  ) {

    setPanels(angle);

    delay(15);
  }

  panelsOpen = false;

  Serial.println(
      "PANELS CLOSED"
  );
}

// ==================================================
// TEST MODE
// ==================================================

void runTest() {

  Serial.println(
      "RUN TEST"
  );

  moveHolo(60, 60);

  delay(1000);

  moveHolo(90, 90);

  delay(1000);

  moveHolo(120, 120);

  delay(1000);

  moveHolo(90, 90);

  delay(1000);

  openTop();

  delay(1500);

  closeTop();

  delay(1500);

  rainbowMode();

  delay(3000);

  leiaMode();

  delay(3000);

  holoLightsOn();

  delay(1000);

  // centerHolos();

  delay(1000);

  rearLogicMessage(
      "R2 READY"
  );

  Serial.println(
      "TEST COMPLETE"
  );
}
// ==================================================
// FRONT HOLO ONLY
// ==================================================

void frontHoloOn() {

  sendRawCommand(
      "*ON01"
  );

  Serial.println(
      "FRONT HOLO ON"
  );
}

void frontHoloOff() {

  sendRawCommand(
      "*OF01"
  );

  Serial.println(
      "FRONT HOLO OFF"
  );
}

// ==================================================
// REAR HOLO ONLY
// ==================================================

void rearHoloOn() {
  rearholoLightsEnabled = true;

  sendRawCommand(
      "*ON02"
  );

  Serial.println(
      "REAR HOLO ON"
  );
}

void rearHoloOff() {
    rearholoLightsEnabled = false;

  sendRawCommand(
      "*OF02"
  );

  Serial.println(
      "REAR HOLO OFF"
  );
}

// ==================================================
// TOP HOLO ONLY
// ==================================================

void topHoloOn() {

  sendRawCommand(
      "*ON03"
  );

  Serial.println(
      "TOP HOLO ON"
  );
}

void topHoloOff() {

  sendRawCommand(
      "*OF03"
  );

  Serial.println(
      "TOP HOLO OFF"
  );
}

// ==================================================
// COMMAND PARSER
// ==================================================

void handleCommand(
    String cmd
) {

  cmd.trim();

  Serial.print(
      "CMD: "
  );

  Serial.println(cmd);

  // ==========================================
  // PANELS
  // ==========================================

  if (cmd == "OPEN_TOP") {

    openTop();
  }

  else if (cmd == "CLOSE_TOP") {

    closeTop();
  }

  // ==========================================
  // TEST
  // ==========================================

  else if (cmd == "TEST") {

    runTest();
  }

  // ==========================================
  // HOLOS
  // ==========================================

  else if (
      cmd == "HOLO_LIGHTS_ON"
  ) {

    holoLightsOn();
  }

  else if (
      cmd == "HOLO_LIGHTS_OFF"
  ) {

    holoLightsOff();
  }
  else if (
    cmd == "FRONT_HOLO"
) {

  frontHoloOn();
}

else if (
    cmd == "FRONT_HOLO_OFF"
) {

  frontHoloOff();
}

else if (
    cmd == "REAR_HOLO"
) {

  rearHoloOn();
}

else if (
    cmd == "REAR_HOLO_OFF"
) {

  rearHoloOff();
}

else if (
    cmd == "TOP_HOLO"
) {

  topHoloOn();
}

else if (
    cmd == "TOP_HOLO_OFF"
) {

  topHoloOff();
}

  else if (
      cmd == "LEIA_MODE"
  ) {

    leiaMode();
  }

  else if (
      cmd == "RAINBOW_MODE"
  ) {

    rainbowMode();
  }

  else if (
      cmd == "TWITCH_ON"
  ) {

    enableTwitch();
  }

  else if (
      cmd == "TWITCH_OFF"
  ) {

    disableTwitch();
  }

  // else if (
  //     cmd == "CENTER_HOLOS"
  // ) {

  //   centerHolos();
  // }

  // ==========================================
  // REAR LOGIC DISPLAY
  // ==========================================

  else if (
      cmd.startsWith("RLD:")
  ) {

    String message =
        cmd.substring(4);

    rearLogicMessage(
        message
    );
  }

  // ==========================================
  // JOYSTICK
  // ==========================================

  else if (
      cmd.startsWith("HOLO:")
  ) {

    int commaIndex =
        cmd.indexOf(',');

    if (commaIndex > 0) {

      int pan =
          cmd.substring(
              5,
              commaIndex
          ).toInt();

      int tilt =
          cmd.substring(
              commaIndex + 1
          ).toInt();

      moveHolo(
          pan,
          tilt
      );
    }
  }
}

// ==================================================
// SETUP
// ==================================================

void setup() {

  Serial.begin(115200);

  Serial.println();
  Serial.println(
      "R2 SYSTEM STARTING"
  );

  // ==========================================
  // BLUETOOTH
  // ==========================================

  SerialBT.begin(
      "R2_SERVO"
  );

  Serial.println(
      "BLUETOOTH READY"
  );

  // ==========================================
  // ASTROPIXELS SERIAL2
  // ==========================================

  AstroSerial.begin(
      9600,
      SERIAL_8N1,
      16,
      17
  );

  delay(3000);

  // ==========================================
  // ENABLE REMOTE MODE
  // ==========================================

  AstroSerial.print(
      "#APREMOTE1\r"
  );

  Serial.println(
      "ASTRO REMOTE ENABLED"
  );

  delay(1000);

  // ==========================================
  // I2C
  // ==========================================

  Wire.begin();

  // ==========================================
  // PCA9685
  // ==========================================

  pwm.begin();

  pwm.setPWMFreq(50);

  delay(1000);

  // ==========================================
  // CENTER HOLO
  // ==========================================

  moveHolo(
      90,
      90
  );

  delay(500);

  // ==========================================
  // CLOSE PANELS
  // ==========================================


  closeTop();

  delay(500);

  // ==========================================
  // ASTRO STARTUP
  // ==========================================


  delay(500);

  holoLightsOn();

  delay(500);

  enableTwitch();

  delay(500);

  rearLogicMessage(
      "R2 READY"
  );

  Serial.println(
      "R2 SYSTEM READY"
  );
}

// ==================================================
// LOOP
// ==================================================

void loop() {

  while (
      SerialBT.available()
  ) {

    char c =
        SerialBT.read();

    if (c == '\n') {

      handleCommand(
          incoming
      );

      incoming = "";

    } else {

      incoming += c;
    }
  }
}