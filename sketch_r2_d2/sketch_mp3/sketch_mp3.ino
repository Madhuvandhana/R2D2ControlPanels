#include <HardwareSerial.h>
#include "BluetoothSerial.h"
#include "esp_bt_device.h"

HardwareSerial mp3Serial(1);

//==================================================
// BENCH SETUP
//==================================================

// Bench
#define MP3_TX_PIN 32      // ESP32 TX -> MP3 RX
#define MP3_RX_PIN 33      // ESP32 RX <- MP3 TX

String incoming = "";

BluetoothSerial SerialBT;

/*
// Uncomment these for the final R2 wiring

#define MP3_TX_PIN 32
#define MP3_RX_PIN 33
*/

uint8_t volumePercent = 80;   // 0-100%



//==================================================
// SOUND TRACK LISTS
//==================================================

const uint8_t greetingTracks[] = {
    1, 2, 3, 4
};

const uint8_t agreementTracks[] = {
    10, 11
};

const uint8_t curiosityTracks[] = {
    20, 21
};

const uint8_t confusionTracks[] = {
    30
};

const uint8_t disbeliefTracks[] = {
    40
};

const uint8_t thinkingTracks[] = {
    50
};

const uint8_t proudTracks[] = {
    60
};

const uint8_t happyTracks[] = {
    70
};

const uint8_t concernedTracks[] = {
    80
};

const uint8_t warningTracks[] = {
    90
};

const uint8_t scaredTracks[] = {
    100
};

const uint8_t embarrassedTracks[] = {
    110
};

const uint8_t teasingTracks[] = {
    120
};

const uint8_t celebratingTracks[] = {
    130
};

const uint8_t investigatingTracks[] = {
    140
};

const uint8_t searchingTracks[] = {
    150
};

const uint8_t foundTracks[] = {
    160
};

const uint8_t drivingTracks[] = {
    170
};

const uint8_t dockingTracks[] = {
    180
};

const uint8_t scanningTracks[] = {
    190
};

const uint8_t listeningTracks[] = {
    200
};

const uint8_t acknowledgedTracks[] = {
    210
};

const uint8_t singingTracks[] = {
    220
};

const uint8_t sleepTracks[] = {
    230
};

const uint8_t wakeTracks[] = {
    240
};

//--------------------------------------------------

void setup() {

  Serial.begin(115200);

    SerialBT.begin(
      "R2_SERVO"
  );

  Serial.print("Bluetooth MAC: ");
    const uint8_t *mac = esp_bt_dev_get_address();

  Serial.printf(
      "Bluetooth MAC: %02X:%02X:%02X:%02X:%02X:%02X\n",
      mac[0], mac[1], mac[2],
      mac[3], mac[4], mac[5]);

mp3Serial.begin(
    38400,
    SERIAL_8N1,
    -1,              // TX only
    MP3_TX_PIN
);

delay(2000);

setVolumePercent(volumePercent);

delay(200);

}

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

void handleCommand(String cmd)
{
    cmd.trim();
    cmd.toUpperCase();

    Serial.print("CMD: ");
    Serial.println(cmd);

    //--------------------------------------------------
    // Sounds
    //--------------------------------------------------

   //--------------------------------------------------
// Sounds
//--------------------------------------------------

    if(cmd == "GREETING")
    {
        playRandomTrack(greetingTracks,
            sizeof(greetingTracks) / sizeof(greetingTracks[0]));
    }

    else if(cmd == "AGREEMENT")
    {
        playRandomTrack(agreementTracks,
            sizeof(agreementTracks) / sizeof(agreementTracks[0]));
    }

    else if(cmd == "CURIOSITY")
    {
        playRandomTrack(curiosityTracks,
            sizeof(curiosityTracks) / sizeof(curiosityTracks[0]));
    }

    else if(cmd == "CONFUSION")
    {
        playRandomTrack(confusionTracks,
            sizeof(confusionTracks) / sizeof(confusionTracks[0]));
    }

    else if(cmd == "DISBELIEF")
    {
        playRandomTrack(disbeliefTracks,
            sizeof(disbeliefTracks) / sizeof(disbeliefTracks[0]));
    }

    else if(cmd == "THINKING")
    {
        playRandomTrack(thinkingTracks,
            sizeof(thinkingTracks) / sizeof(thinkingTracks[0]));
    }

    else if(cmd == "PROUD")
    {
        playRandomTrack(proudTracks,
            sizeof(proudTracks) / sizeof(proudTracks[0]));
    }

    else if(cmd == "HAPPY")
    {
        playRandomTrack(happyTracks,
            sizeof(happyTracks) / sizeof(happyTracks[0]));
    }

    else if(cmd == "CONCERNED")
    {
        playRandomTrack(concernedTracks,
            sizeof(concernedTracks) / sizeof(concernedTracks[0]));
    }

    else if(cmd == "WARNING")
    {
        playRandomTrack(warningTracks,
            sizeof(warningTracks) / sizeof(warningTracks[0]));
    }

    else if(cmd == "SCARED")
    {
        playRandomTrack(scaredTracks,
            sizeof(scaredTracks) / sizeof(scaredTracks[0]));
    }

    else if(cmd == "EMBARRASSED")
    {
        playRandomTrack(embarrassedTracks,
            sizeof(embarrassedTracks) / sizeof(embarrassedTracks[0]));
    }

    else if(cmd == "TEASING")
    {
        playRandomTrack(teasingTracks,
            sizeof(teasingTracks) / sizeof(teasingTracks[0]));
    }

    else if(cmd == "CELEBRATING")
    {
        playRandomTrack(celebratingTracks,
            sizeof(celebratingTracks) / sizeof(celebratingTracks[0]));
    }

    else if(cmd == "INVESTIGATING")
    {
        playRandomTrack(investigatingTracks,
            sizeof(investigatingTracks) / sizeof(investigatingTracks[0]));
    }

    else if(cmd == "SEARCHING")
    {
        playRandomTrack(searchingTracks,
            sizeof(searchingTracks) / sizeof(searchingTracks[0]));
    }

    else if(cmd == "FOUND")
    {
        playRandomTrack(foundTracks,
            sizeof(foundTracks) / sizeof(foundTracks[0]));
    }

    else if(cmd == "DRIVING")
    {
        playRandomTrack(drivingTracks,
            sizeof(drivingTracks) / sizeof(drivingTracks[0]));
    }

    else if(cmd == "DOCKING")
    {
        playRandomTrack(dockingTracks,
            sizeof(dockingTracks) / sizeof(dockingTracks[0]));
    }

    else if(cmd == "SCANNING")
    {
        playRandomTrack(scanningTracks,
            sizeof(scanningTracks) / sizeof(scanningTracks[0]));
    }

    else if(cmd == "LISTENING")
    {
        playRandomTrack(listeningTracks,
            sizeof(listeningTracks) / sizeof(listeningTracks[0]));
    }

    else if(cmd == "ACKNOWLEDGED")
    {
        playRandomTrack(acknowledgedTracks,
            sizeof(acknowledgedTracks) / sizeof(acknowledgedTracks[0]));
    }

    else if(cmd == "SINGING")
    {
        playRandomTrack(singingTracks,
            sizeof(singingTracks) / sizeof(singingTracks[0]));
    }

    else if(cmd == "SLEEP")
    {
        playRandomTrack(sleepTracks,
            sizeof(sleepTracks) / sizeof(sleepTracks[0]));
    }

    else if(cmd == "WAKE")
    {
        playRandomTrack(wakeTracks,
            sizeof(wakeTracks) / sizeof(wakeTracks[0]));
    }

      else if(cmd == "SCREAM")
    {
        playTrack(254);
    }

    //--------------------------------------------------
    // Music
    //--------------------------------------------------

    else if(cmd == "STAR_WARS")
        playTrack(250);

    else if(cmd == "DISCO")
        playTrack(253);

    else if(cmd == "MARCH")
        playTrack(252);

    else if(cmd == "CANTINA")
        playTrack(251);

    //--------------------------------------------------
    // Volume
    //--------------------------------------------------

    else if(cmd == "STOP")
        stopTrack();

    else if(cmd.startsWith("VOLUME:"))
    {
        int v = cmd.substring(7).toInt();
        setVolumePercent(v);
    }

    else
    {
        Serial.println("Unknown command");
    }
}
//==================================================
// MP3 Trigger Commands
//==================================================

// Play by filename:
// 001xxxxx.mp3
// 002xxxxx.mp3
// ...
// 253xxxxx.mp3

void playTrack(uint8_t track)
{
    mp3Serial.write('t');
    mp3Serial.write(track);
    mp3Serial.flush();
    delay(75);
}

void playRandomTrack(const uint8_t tracks[], size_t count)
{
    uint8_t track = tracks[random(count)];

    Serial.print("Random Track = ");
    Serial.println(track);

    playTrack(track);
}

void stopTrack()
{
    mp3Serial.write('O');
    mp3Serial.flush();
    delay(50);
}

void setVolumePercent(uint8_t percent)
{
    percent = constrain(percent, 0, 100);

    volumePercent = percent;

    // Convert Android 0-100% -> MP3 Trigger 255-0
    uint8_t mp3Volume = map(percent, 0, 100, 255, 0);

    mp3Serial.write('v');
    mp3Serial.write(mp3Volume);
    mp3Serial.flush();

    delay(75);

    Serial.print("Volume ");
    Serial.print(percent);
    Serial.print("% -> MP3=");
    Serial.println(mp3Volume);
}