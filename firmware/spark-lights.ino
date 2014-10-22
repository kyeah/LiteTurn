#include "application.h"
//#include "spark_disable_wlan.h" // For faster local debugging only
#include "neopixel.h"

// IMPORTANT: Set pixel COUNT, PIN and TYPE
#define PIXEL_PIN D0
#define PIXEL_COUNT 24
#define PIXEL_TYPE WS2812B

Adafruit_NeoPixel strip = Adafruit_NeoPixel(PIXEL_COUNT, PIXEL_PIN, PIXEL_TYPE);

//Initialize to Red
uint32_t color = strip.Color(255, 0, 0);
boolean rightFlash = false;
boolean leftFlash = false;

void setup() 
{
  strip.begin();
  strip.setBrightness(255);
  strip.show(); // Initialize all pixels to 'off'
  Spark.function("on", ledControl);
  Spark.function("off", offControl);
  Spark.function("setColor", colorControl);
  Spark.variable("color", &color, INT);
}

int colorControl(String command) {
    uint16_t i;
    if (command.length() != 11) {
        return -1;
    }
    
    int r = command.substring(0, 3).toInt();
    int g = command.substring(4, 7).toInt();
    int b = command.substring(8, 10).toInt();
    
    color = strip.Color(r, g, b);
    
    return 1;
}

int offControl(String command) {
    uint16_t i;
    for (i = 0; i < strip.numPixels(); i++) {
        strip.setPixelColor(i, 0);
    }
    leftFlash = false;
    rightFlash = false;
    strip.show();
    return 1;
}

int ledControl(String command) {
    if (command == "LEFT") {
        leftFlash = true;
        rightFlash = false;
    } else if (command == "RIGHT") {
        rightFlash = true;
        leftFlash = false;
    }
    return 1;
}

void loop() {
    uint16_t i;
    
    while(rightFlash) {
        for(i = 0; i < strip.numPixels() / 2; i++) {
            strip.setPixelColor(i, strip.Color(0, 0, 0));
        }
        
        for(i = strip.numPixels() / 2; i < strip.numPixels(); i++) {
            strip.setPixelColor(i, color);
        }
        
        strip.show();
        delay(500);
        
        for(i = 0; i < strip.numPixels(); i++) {
            strip.setPixelColor(i, 0);
        }
        strip.show();
        delay(500);
    }
    
    while(leftFlash) {
        for(i = strip.numPixels() / 2; i < strip.numPixels(); i++) {
            strip.setPixelColor(i, strip.Color(0, 0, 0));
        }
        
        for(i = 0; i < strip.numPixels() / 2; i++) {
            strip.setPixelColor(i, color);
        }
        
        strip.show();
        delay(500);
        
        for(i = 0; i < strip.numPixels(); i++) {
            strip.setPixelColor(i, 0);
        }
        strip.show();
        delay(500);
    }
  //Nothing to do;
}