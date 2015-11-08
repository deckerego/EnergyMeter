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

  // Ratio used for conversion from RMS is: Ic * (V / Rdac)
  // Where Ic is the current calibration coefficient,
  // V is the accepted voltage of the input pin, and
  // Rdac is the resolution of the digital/analog converter.
  // For the current calibration coefficient, I'm assuming
  // a 33 Ω ±1% burden resistor over a current transformer that
  // transforms 100A to 50mA, or: (100A / 0.05A) / 33 Ω = 60.6060...
  // I'm also assuming a 5V Trinket with a 10-bit DAC, and so:
  // Ic * (5V / 10 bits) = 60.6060 * (5.0 / 1024) = 0.29592803030...
  TFUSerial.println(0.2959280303030 * sqrt(sum / 1200));
}
