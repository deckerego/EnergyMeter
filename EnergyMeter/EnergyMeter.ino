// Note that I'm assuming we're using a 5V Adafruit Trinket and
// a SCT-013-000 current transformer sensor below. Values are
// hard-coded to cut down on sketch size.

#include "TrinketFakeUsbSerial.h"

double offset = 512.0; // Assuming a 10-bit DAC, so 1024 / 2 in base10

void setup()
{
  TFUSerial.begin();

  pinMode(1, OUTPUT);
  pinMode(2, INPUT);
}

void loop()
{
  TFUSerial.task();

  double sum = 0.0;
  for (unsigned int n = 0; n < 1200; n++) {
    int voltage = analogRead(1);
    offset = (offset + (voltage - offset) / 1024); // Also assuming a 10-bit DAC
    double filtered = voltage - offset;
    sum += filtered * filtered;
  }

  TFUSerial.println(sqrt(sum / 1200));
}
