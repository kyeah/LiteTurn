LiteTurn
==========

![App Splash Image](http://kyeh.me/img/projects/liteturn-splash.png)

LiteTurn is a gesture recognition and automation system for cheap, accessible, and energy-efficient wireless turn lights. It combines generic accelerometer and gyroscope sensor information from wrist wearables, along with simple GPS localization, to detect user states and communicate with turn lights attached to the user or mounted on the bicycle.

At HackTX 2014, we developed a prototype consumer product that used Myo gesture and orientation sensors to detect turn signal gestures, Android GPS bearing information to detect turn completions, and a wireless Spark Core hooked up to a 24-Neopixel LED ring that acted as turn lights. These products were available at hand, but could be easily replaced by a consumer smartwatch and a cheap microcontroller + bluetooth chip to provide lower energy usage, lower costs, and more accurate recognition. Here is our [demo teaser](https://www.youtube.com/watch?v=QdmPOHyUchk).

The product was further developed as part of the CS386W Wireless Networking course at The University of Texas at Austin. Here is the [final research paper](http://kyeh.me/img/projects/liteturn-final.pdf) and [accompanying presentation](http://kyeh.me/img/projects/liteturn-pres.pdf).

In the future, the hope is to develop LiteTurn into a system that can detect the following activities in real-time:

* ✔ Turn signal gestures
* ✔ Turn completion
* Pedaling
* Braking
* Dismounting
* Oncoming vehicles (distance alerts)
* Accidents
