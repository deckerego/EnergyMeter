#include "TrinketFakeUsbSerial.h"

void setup()
{
  TFUSerial.begin();
  pinMode(1, OUTPUT);
  pinMode(2, INPUT);
}

void loop()
{
  TFUSerial.task();

  int calibrationValue = 508;
  analogWrite(1, (analogRead(1) - calibrationValue) * 2);

  TFUSerial.println(analogRead(1) - calibrationValue);
}
