#!/bin/sh

AIO_KEY='feedfacedeadbeef'
AIO_FEED='yourfeed-key'
AIO_URL="https://io.adafruit.com/api/feeds/$AIO_FEED/data.json"
BASE_URL="http://localhost:9000"
CURRENT_ENDPOINT="$BASE_URL/current"
CURRENT_AMPS=$(/usr/bin/curl $CURRENT_ENDPOINT | /bin/sed 's/.*amperage.\://g' | /bin/sed 's/,.*//g')

/usr/bin/curl -H "X-AIO-Key: $AIO_KEY" -H 'Content-Type: application/json' -X POST $AIO_URL -d"{\"value\": $CURRENT_AMPS}"
