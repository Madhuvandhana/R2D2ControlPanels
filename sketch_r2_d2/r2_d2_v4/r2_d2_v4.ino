#include <Wire.h>
#include <Adafruit_PWMServoDriver.h>
#include "BluetoothSerial.h"
#include <SabertoothSimplified.h>
#include <SoftwareSerial.h>

// Forward declarations for compiler prototypes
void domeLeft();
void domeRight();
void domeStop();
void openTop();
void closeTop();
void runTest();
void holoLightsOn();
void holoLightsOff();
void frontHoloOn();
void frontHoloOff();
void rearHoloOn();
void rearHoloOff();
void topHoloOn();
void topHoloOff();
void leiaMode();
void rainbowMode();
void enableTwitch();
void disableTwitch();
void rearLogicMessage(String msg);
void moveHolo(int pan, int tilt);
void openChargeDoor();
void closeChargeDoor();
void openDataDoor();
void closeDataDoor();
void deployArmA();
void retractArmA();
void deployArmB();
void retractArmB();
void openGripperDoor();
void closeGripperDoor();
void gripperOpen();
void gripperClose();
void openInterfaceDoor();
void closeInterfaceDoor();
void happyMode();
void sadMode();
void excitedMode();
void danceMode();

// TX only (connect to S1 on Sabertooth) - using SoftwareSerial
#define SABER_TX 27
#define SABER_RX -1

SoftwareSerial STSerial(SABER_RX, SABER_TX);
SabertoothSimplified ST(STSerial);

// ==================================================
// BLUETOOTH
// ==================================================

BluetoothSerial SerialBT;

// ==================================================
// MP3 SERIAL (HARDWARE SERIAL 1)
// ==================================================
#define MP3_TX_PIN 32
HardwareSerial mp3Serial(1);
uint8_t volumePercent = 80;   // Default 80%

const uint8_t greetingTracks[]     = { 1, 2, 3, 4 };
const uint8_t agreementTracks[]    = { 10, 11 };
const uint8_t curiosityTracks[]    = { 20, 21 };
const uint8_t confusionTracks[]    = { 30 };
const uint8_t disbeliefTracks[]    = { 40 };
const uint8_t thinkingTracks[]     = { 50 };
const uint8_t proudTracks[]        = { 60 };
const uint8_t happyTracks[]        = { 70 };
const uint8_t concernedTracks[]    = { 80 };
const uint8_t warningTracks[]      = { 90 };
const uint8_t scaredTracks[]       = { 100 };
const uint8_t embarrassedTracks[]  = { 110 };
const uint8_t teasingTracks[]      = { 120 };
const uint8_t celebratingTracks[]  = { 130 };
const uint8_t investigatingTracks[]= { 140 };
const uint8_t searchingTracks[]    = { 150 };
const uint8_t foundTracks[]        = { 160 };
const uint8_t drivingTracks[]      = { 170 };
const uint8_t dockingTracks[]      = { 180 };
const uint8_t scanningTracks[]     = { 190 };
const uint8_t listeningTracks[]    = { 200 };
const uint8_t acknowledgedTracks[] = { 210 };
const uint8_t singingTracks[]      = { 220 };
const uint8_t sleepTracks[]        = { 230 };
const uint8_t wakeTracks[]         = { 240 };

// ==================================================
// ASTROPIXELS SERIAL2
// ==================================================

HardwareSerial AstroSerial(2);

// ==================================================
// PCA9685
// ==================================================

// Dome PCA9685
Adafruit_PWMServoDriver domePwm =
    Adafruit_PWMServoDriver(0x40);

// Body PCA9685
Adafruit_PWMServoDriver bodyPwm =
    Adafruit_PWMServoDriver(0x41);
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

#define PANEL_CLOSED  170
#define PANEL_OPEN    40

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

// ==========================================
// DOME MOTOR
// ==========================================

#define DOME_PWM_PIN 25
#define DOME_DIR_PIN 26

#define DOME_PWM_CHANNEL 0
#define DOME_SPEED 160 // Speed value from 0 to 255 (increased from 60 for heavy loads)

// ==========================================
// AS5600
// ==========================================

#define AS5600_ADDR 0x36


// ==========================================
// BODY PCA9685 CHANNELS
// ==========================================

#define ARM_A_LIFT        0
#define ARM_B_LIFT        1

#define GRIPPER_LIFT      2
#define GRIPPER_OPEN      3

#define INTERFACE_LIFT    4
#define INTERFACE_ROTATE  5

#define INTERFACE_DOOR    6
#define GRIPPER_DOOR      7

#define CHARGE_DOOR       8
#define DATA_DOOR         9


#define SERVO_OPEN_POS      150
#define SERVO_CLOSED_POS     30

#define ARM_DEPLOY_POS      30
#define ARM_RETRACT_POS      160

#define ARM_B_DEPLOY_POS      30
#define ARM_B_RETRACT_POS      180

#define DOOR_OPEN_POS      120
#define DOOR_CLOSE_POS      30

#define DOOR_B_OPEN_POS      45
#define DOOR_B_CLOSE_POS      100

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

  Serial.printf("[DEBUG] Dome Servo Ch %d -> Angle %d\n", channel, angle);

  int pulse = map(
      angle,
      0,
      180,
      SERVOMIN,
      SERVOMAX
  );

  domePwm.setPWM(
      channel,
      0,
      pulse
  );
}

void setBodyServo(
    int channel,
    int angle
)
{
    angle = constrain(angle, 0, 180);

    Serial.printf("[DEBUG] Body Servo Ch %d -> Angle %d\n", channel, angle);

    int pulse = map(
        angle,
        0,
        180,
        SERVOMIN,
        SERVOMAX
    );

    bodyPwm.setPWM(
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

void happyMode() {
  rearLogicMessage("HAPPY");
  playRandomTrack(happyTracks, sizeof(happyTracks) / sizeof(happyTracks[0]));
  
  domeLeft();
  delay(300);
  domeRight();
  delay(300);
  domeStop();

  openTop();
  delay(300);
  closeTop();
}

void sadMode() {
  rearLogicMessage("SAD");
  playRandomTrack(concernedTracks, sizeof(concernedTracks) / sizeof(concernedTracks[0]));
  
  moveHolo(90, 150);
  
  digitalWrite(DOME_DIR_PIN, LOW);
  ledcWrite(DOME_PWM_PIN, DOME_SPEED / 2);
  delay(400);
  domeStop();
  
  delay(1000);
  moveHolo(90, 90);
}

void excitedMode() {
  rearLogicMessage("EXCITED");
  playRandomTrack(celebratingTracks, sizeof(celebratingTracks) / sizeof(celebratingTracks[0]));
  
  openTop();
  
  for (int i = 0; i < 3; i++) {
    moveHolo(70, 70);
    domeLeft();
    delay(200);
    moveHolo(110, 110);
    domeRight();
    delay(200);
  }
  
  domeStop();
  moveHolo(90, 90);
  closeTop();
}

void danceMode() {
  rearLogicMessage("DANCE");
  playTrack(253);
  
  for (int step = 0; step < 4; step++) {
    openTop();
    domeLeft();
    moveHolo(60, 90);
    delay(500);
    
    closeTop();
    domeRight();
    moveHolo(120, 90);
    delay(500);
  }
  
  domeStop();
  moveHolo(90, 90);
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

void detachDomePanels() {
  domePwm.setPWM(PANEL_1, 0, 4096);
  domePwm.setPWM(PANEL_2, 0, 4096);
  domePwm.setPWM(PANEL_3, 0, 4096);
  domePwm.setPWM(PANEL_4, 0, 4096);
}

// ==================================================
// OPEN PANELS
// ==================================================

void openTop() {

  // domeStop();

  if (panelsOpen) {
    return;
  }

  for (

      int angle = PANEL_CLOSED;

      angle >= PANEL_OPEN;

      angle -= 1

  ) {

    setPanels(angle);

    delay(25);
  }

  panelsOpen = true;

  Serial.println(
      "PANELS OPEN"
  );

  delay(300);
  detachDomePanels();
  // domeStop();
}

// ==================================================
// CLOSE PANELS
// ==================================================

void closeTop() {

  // domeStop();

  if (!panelsOpen) {
    return;
  }

  for (

      int angle = PANEL_OPEN;

      angle <= PANEL_CLOSED;

      angle += 1

  ) {

    setPanels(angle);

    delay(25);
  }

  panelsOpen = false;

  Serial.println(
      "PANELS CLOSED"
  );

  delay(300);
  detachDomePanels();
  // domeStop();
}

void openChargeDoor() {
  setBodyServo(CHARGE_DOOR, SERVO_OPEN_POS);
}

void closeChargeDoor() {
  setBodyServo(CHARGE_DOOR, SERVO_CLOSED_POS);
}

void openDataDoor() {
  setBodyServo(DATA_DOOR, SERVO_OPEN_POS);
}

void closeDataDoor() {
  setBodyServo(DATA_DOOR, SERVO_CLOSED_POS);
}

void openGripperDoor() {
  setBodyServo(GRIPPER_DOOR, DOOR_OPEN_POS);
}

void closeGripperDoor() {
  setBodyServo(GRIPPER_DOOR, DOOR_CLOSE_POS);
}

void openInterfaceDoor() {
  setBodyServo(INTERFACE_DOOR, DOOR_B_OPEN_POS);
}

void closeInterfaceDoor() {
  setBodyServo(INTERFACE_DOOR, DOOR_B_CLOSE_POS);
}


void deployArmA() {
  setBodyServo(ARM_A_LIFT, ARM_DEPLOY_POS);
}

void retractArmA() {
  setBodyServo(ARM_A_LIFT, ARM_RETRACT_POS);
}

void deployArmB() {
  setBodyServo(ARM_B_LIFT, ARM_B_DEPLOY_POS);
}

void retractArmB() {
  setBodyServo(ARM_B_LIFT, ARM_B_RETRACT_POS);
}

void gripperOpen() {
  setBodyServo(GRIPPER_OPEN, SERVO_OPEN_POS);
}

void gripperClose() {
  setBodyServo(GRIPPER_OPEN, SERVO_CLOSED_POS);
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
// SABERTOOTH TANK DRIVE RAMPING CONTROL
// ==================================================

// Target speeds set by incoming commands
int targetLeft = 0;
int targetRight = 0;

// Current motor speeds ramped progressively
int currentLeft = 0;
int currentRight = 0;

// Ramping acceleration limit (maximum speed change units per 20ms update)
// Higher values = quicker response; lower values = smoother acceleration
#define ACCEL_LIMIT 3

void stopMotors()
{
    targetLeft = 0;
    targetRight = 0;
    currentLeft = 0;
    currentRight = 0;
    ST.motor(1, 0);
    ST.motor(2, 0);
}

void drive(int left, int right)
{
    // Swap and invert channels to correct motor wiring directions
    int newLeft = -right;
    int newRight = -left;

    // Constrain to Sabertooth Simplified limits: -127 to 127
    targetLeft = constrain(newLeft, -127, 127);
    targetRight = constrain(newRight, -127, 127);

    Serial.printf("Target L:%d  R:%d\n", targetLeft, targetRight);
}

// Periodically runs inside loop() to smooth speed transitions
void updateRamping()
{
    static unsigned long lastRampTime = 0;
    unsigned long now = millis();
    
    if (now - lastRampTime >= 20) // Update every 20ms
    {
        lastRampTime = now;
        bool changed = false;

        // Ramp Left Motor
        if (currentLeft < targetLeft) {
            currentLeft = min(currentLeft + ACCEL_LIMIT, targetLeft);
            changed = true;
        } else if (currentLeft > targetLeft) {
            currentLeft = max(currentLeft - ACCEL_LIMIT, targetLeft);
            changed = true;
        }

        // Ramp Right Motor
        if (currentRight < targetRight) {
            currentRight = min(currentRight + ACCEL_LIMIT, targetRight);
            changed = true;
        } else if (currentRight > targetRight) {
            currentRight = max(currentRight - ACCEL_LIMIT, targetRight);
            changed = true;
        }

        // Send speed updates to Sabertooth if speed values changed
        if (changed) {
            ST.motor(1, currentLeft);
            ST.motor(2, currentRight);
        }
    }
}

void playTrack(uint8_t track) {
  mp3Serial.write('t');
  mp3Serial.write(track);
  mp3Serial.flush();
  delay(75);
}

void playRandomTrack(const uint8_t tracks[], size_t count) {
  uint8_t track = tracks[random(count)];
  Serial.print("Random Track = ");
  Serial.println(track);
  playTrack(track);
}

void stopTrack() {
  mp3Serial.write('O');
  mp3Serial.flush();
  delay(50);
}

void setVolumePercent(uint8_t percent) {
  percent = constrain(percent, 0, 100);
  volumePercent = percent;
  uint8_t mp3Volume = map(percent, 0, 100, 255, 0);
  mp3Serial.write('v');
  mp3Serial.write(mp3Volume);
  mp3Serial.flush();
  delay(75);
  Serial.printf("Volume %d%% -> MP3=%d\n", percent, mp3Volume);
}

// ==================================================
// COMMAND PARSER
// ==================================================

void handleCommand(
    String cmd
) {

  cmd.trim();
  cmd.toUpperCase();

  Serial.print(
      "CMD: "
  );

  Serial.println(cmd);

  if (cmd == "STOP")
  {
      stopMotors();
  }
  else if (cmd == "STOP_AUDIO")
  {
      stopTrack();
  }
  else if (cmd.startsWith("DRIVE:"))
  {
      int comma = cmd.indexOf(',');

      if (comma > 0)
      {
          int left = cmd.substring(6, comma).toInt();
          int right = cmd.substring(comma + 1).toInt();

          drive(left, right);
      }
  }
  // ==========================================
  // PANELS
  // ==========================================

  else if (cmd == "OPEN_TOP") {

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
      cmd == "HAPPY_MODE"
  ) {
    happyMode();
  }

  else if (
      cmd == "SAD_MODE"
  ) {
    sadMode();
  }

  else if (
      cmd == "EXCITED_MODE"
  ) {
    excitedMode();
  }

  else if (
      cmd == "DANCE_MODE"
  ) {
    danceMode();
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
  else if(cmd == "DOME_LEFT")
  {
      domeLeft();
  }

  else if(cmd == "DOME_RIGHT")
  {
      domeRight();
  }

  else if(cmd == "DOME_STOP")
  {
      domeStop();
  }

  else if(cmd == "DOME_HOME")
  {
      domeStop();
  }

  else if(cmd == "CHARGE_OPEN")
{
    openChargeDoor();
}

else if(cmd == "CHARGE_CLOSE")
{
    closeChargeDoor();
}

else if(cmd == "DATA_OPEN")
{
    openDataDoor();
}

else if(cmd == "DATA_CLOSE")
{
    closeDataDoor();
}

else if(cmd == "ARM_A_DEPLOY")
{
    deployArmA();
}

else if(cmd == "ARM_A_RETRACT")
{
    retractArmA();
}

else if(cmd == "ARM_B_DEPLOY")
{
    deployArmB();
}

else if(cmd == "ARM_B_RETRACT")
{
    retractArmB();
}

else if(cmd == "GRIPPER_OPEN")
{
    openGripperDoor();
    delay(200);
    gripperOpen();
}

else if(cmd == "GRIPPER_CLOSE")
{
    gripperClose();
    delay(200);
    closeGripperDoor();
}

else if(cmd == "INTERFACE_OPEN")
{
    openInterfaceDoor();
}

else if(cmd == "INTERFACE_CLOSE")
{
    closeInterfaceDoor();
}

  // ==========================================
  // MP3 TRIGGER SOUND COMMANDS
  // ==========================================
  else if(cmd == "GREETING")
  {
      playRandomTrack(greetingTracks, sizeof(greetingTracks) / sizeof(greetingTracks[0]));
  }
  else if(cmd == "AGREEMENT")
  {
      playRandomTrack(agreementTracks, sizeof(agreementTracks) / sizeof(agreementTracks[0]));
  }
  else if(cmd == "CURIOSITY")
  {
      playRandomTrack(curiosityTracks, sizeof(curiosityTracks) / sizeof(curiosityTracks[0]));
  }
  else if(cmd == "CONFUSION")
  {
      playRandomTrack(confusionTracks, sizeof(confusionTracks) / sizeof(confusionTracks[0]));
  }
  else if(cmd == "DISBELIEF")
  {
      playRandomTrack(disbeliefTracks, sizeof(disbeliefTracks) / sizeof(disbeliefTracks[0]));
  }
  else if(cmd == "THINKING")
  {
      playRandomTrack(thinkingTracks, sizeof(thinkingTracks) / sizeof(thinkingTracks[0]));
  }
  else if(cmd == "PROUD")
  {
      playRandomTrack(proudTracks, sizeof(proudTracks) / sizeof(proudTracks[0]));
  }
  else if(cmd == "HAPPY")
  {
      playRandomTrack(happyTracks, sizeof(happyTracks) / sizeof(happyTracks[0]));
  }
  else if(cmd == "CONCERNED")
  {
      playRandomTrack(concernedTracks, sizeof(concernedTracks) / sizeof(concernedTracks[0]));
  }
  else if(cmd == "WARNING")
  {
      playRandomTrack(warningTracks, sizeof(warningTracks) / sizeof(warningTracks[0]));
  }
  else if(cmd == "SCARED")
  {
      playRandomTrack(scaredTracks, sizeof(scaredTracks) / sizeof(scaredTracks[0]));
  }
  else if(cmd == "EMBARRASSED")
  {
      playRandomTrack(embarrassedTracks, sizeof(embarrassedTracks) / sizeof(embarrassedTracks[0]));
  }
  else if(cmd == "TEASING")
  {
      playRandomTrack(teasingTracks, sizeof(teasingTracks) / sizeof(teasingTracks[0]));
  }
  else if(cmd == "CELEBRATING")
  {
      playRandomTrack(celebratingTracks, sizeof(celebratingTracks) / sizeof(celebratingTracks[0]));
  }
  else if(cmd == "INVESTIGATING")
  {
      playRandomTrack(investigatingTracks, sizeof(investigatingTracks) / sizeof(investigatingTracks[0]));
  }
  else if(cmd == "SEARCHING")
  {
      playRandomTrack(searchingTracks, sizeof(searchingTracks) / sizeof(searchingTracks[0]));
  }
  else if(cmd == "FOUND")
  {
      playRandomTrack(foundTracks, sizeof(foundTracks) / sizeof(foundTracks[0]));
  }
  else if(cmd == "DRIVING")
  {
      playRandomTrack(drivingTracks, sizeof(drivingTracks) / sizeof(drivingTracks[0]));
  }
  else if(cmd == "DOCKING")
  {
      playRandomTrack(dockingTracks, sizeof(dockingTracks) / sizeof(dockingTracks[0]));
  }
  else if(cmd == "SCANNING")
  {
      playRandomTrack(scanningTracks, sizeof(scanningTracks) / sizeof(scanningTracks[0]));
  }
  else if(cmd == "LISTENING")
  {
      playRandomTrack(listeningTracks, sizeof(listeningTracks) / sizeof(listeningTracks[0]));
  }
  else if(cmd == "ACKNOWLEDGED")
  {
      playRandomTrack(acknowledgedTracks, sizeof(acknowledgedTracks) / sizeof(acknowledgedTracks[0]));
  }
  else if(cmd == "SINGING")
  {
      playRandomTrack(singingTracks, sizeof(singingTracks) / sizeof(singingTracks[0]));
  }
  else if(cmd == "SLEEP")
  {
      playRandomTrack(sleepTracks, sizeof(sleepTracks) / sizeof(sleepTracks[0]));
  }
  else if(cmd == "WAKE")
  {
      playRandomTrack(wakeTracks, sizeof(wakeTracks) / sizeof(wakeTracks[0]));
  }
  else if(cmd == "SCREAM")
  {
      playTrack(254);
  }
  else if(cmd == "STAR_WARS")
  {
      playTrack(250);
  }
  else if(cmd == "DISCO")
  {
      playTrack(253);
  }
  else if(cmd == "MARCH")
  {
      playTrack(252);
  }
  else if(cmd == "CANTINA")
  {
      playTrack(251);
  }
  else if(cmd.startsWith("VOLUME:"))
  {
      int v = cmd.substring(7).toInt();
      setVolumePercent(v);
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
  // MP3 TRIGGER SETUP (HARDWARE SERIAL 1)
  // ==========================================
  mp3Serial.begin(
      38400,
      SERIAL_8N1,
      -1,              // TX only
      MP3_TX_PIN
  );
  delay(10);
  setVolumePercent(volumePercent);

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

  // ==========================================
  // SABERTOOTH (SOFTWARE SERIAL)
  // ==========================================

  STSerial.begin(9600);

  // Send autobaud character (0xAA) to sync Sabertooth baud rate
  delay(1000); 
  STSerial.write(0xAA);
  delay(100);

  stopMotors();

  Serial.println(
      "SABERTOOTH MOTOR CONTROLLER READY"
  );

  delay(2000);

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

  domePwm.begin();

  domePwm.setPWMFreq(50);

  bodyPwm.begin();
  bodyPwm.setPWMFreq(50);

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

  pinMode(DOME_DIR_PIN, OUTPUT);

  ledcAttach(
    DOME_PWM_PIN,
    20000,
    8
  );

  ledcWrite(
    DOME_PWM_PIN,
    0
  );

  Serial.println("[DEBUG] Scanning I2C Bus for PCA9685 boards...");
  bool domeBoardFound = false;
  bool bodyBoardFound = false;

  for(byte addr=1; addr<127; addr++)
  {
      Wire.beginTransmission(addr);

      if(Wire.endTransmission()==0)
      {
          Serial.print("[DEBUG] Found I2C Device at: 0x");
          Serial.println(addr, HEX);
          if (addr == 0x40) {
              Serial.println("  -> SUCCESS: PCA9685 Dome Board (0x40) Detected!");
              domeBoardFound = true;
          }
          else if (addr == 0x41) {
              Serial.println("  -> SUCCESS: PCA9685 Body Board (0x41) Detected!");
              bodyBoardFound = true;
          }
      }
  }

  if (!domeBoardFound) {
      Serial.println("[ERROR] WARNING: PCA9685 Dome Board (0x40) NOT detected on I2C bus!");
  }
  if (!bodyBoardFound) {
      Serial.println("[ERROR] WARNING: PCA9685 Body Board (0x41) NOT detected on I2C bus!");
  }

// domeGoto(0);

}


void domeStop() {

  ledcWrite(
      DOME_PWM_PIN,
      0
  );
}

void domeLeft() {

  digitalWrite(
      DOME_DIR_PIN,
      LOW
  );

  ledcWrite(
      DOME_PWM_PIN,
      DOME_SPEED
  );
}

void domeRight() {

  digitalWrite(
      DOME_DIR_PIN,
      HIGH
  );

  ledcWrite(
      DOME_PWM_PIN,
      DOME_SPEED
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
      
      // Buffer guard: discard if command is malformed or too long (prevents overflow)
      if (incoming.length() > 64) {
        incoming = "";
      }
    }
  }

  // Run the non-blocking motor ramping loop
  updateRamping();
}