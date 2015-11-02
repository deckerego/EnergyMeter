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

  analogWrite(1, 0);
  delay(100);

  analogWrite(1, analogRead(2));

  TFUSerial.println(analogRead(2));
}
