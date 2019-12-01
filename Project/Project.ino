#include <MQ135.h>
#include <SoftwareSerial.h>

SoftwareSerial BTSerial(10, 11);

float AQ = 0;

void setup() {
  //Serial.begin(9600);
  BTSerial.begin(9600);
}

void loop() {
  MQ135 GS = MQ135(A0); //GS = Gas Sensor
  AQ = GS.getPPM(); //AQ = Air Quality
  
  Serial.print("Air quality: ");
  Serial.print(AQ);
  Serial.println(" ppm");

  BTSerial.print(AQ);
  BTSerial.println(" ppm");
  
  delay(1000);
}
