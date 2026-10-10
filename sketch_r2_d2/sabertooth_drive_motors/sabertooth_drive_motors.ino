#include "BluetoothSerial.h"
#include <SabertoothSimplified.h>

BluetoothSerial SerialBT;

// UART1 for Sabertooth
HardwareSerial STSerial(1);

// TX only (connect to S1 on Sabertooth)
#define SABER_TX 27
#define SABER_RX -1

SabertoothSimplified ST(STSerial);

String incoming = "";

// DIP Switch Settings for Sabertooth (Simplified Serial @ 9600 Baud):
// 1: OFF (if using Lithium) or ON (if using Lead-Acid/NiMH)
// 2: ON  (Simplified Serial Mode)
// 3: ON  (No lithium cutoff) or OFF (Lithium cutoff enabled)
// 4: OFF (9600 Baud)
// 5: ON  (9600 Baud)
// 6: ON  (Standard Simplified Serial)

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
    // Constrain to Sabertooth Simplified limits: -127 to 127
    targetLeft = constrain(left, -127, 127);
    targetRight = constrain(right, -127, 127);

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

void handleCommand(String cmd)
{
    cmd.trim();

    Serial.println("Received: " + cmd);

    if (cmd == "STOP")
    {
        stopMotors();
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
}

void setup()
{
    Serial.begin(115200);

    Serial.println();
    Serial.println("Sabertooth Bench Test Starting...");

    SerialBT.begin("R2_DRIVE");

    STSerial.begin(
        9600,
        SERIAL_8N1,
        SABER_RX,
        SABER_TX
    );

    // Send autobaud character (0xAA) to sync Sabertooth baud rate
    delay(2000); // Give Sabertooth time to boot up
    STSerial.write(0xAA);
    delay(100);

    stopMotors();

    Serial.println("Bluetooth & Sabertooth Ready");
}

void loop()
{
    while (SerialBT.available())
    {
        char c = SerialBT.read();

        if (c == '\n')
        {
            handleCommand(incoming);
            incoming = "";
        }
        else
        {
            incoming += c;
        }
    }

    // Run the non-blocking motor ramping loop
    updateRamping();
}