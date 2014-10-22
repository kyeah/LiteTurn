LiteTurn
==========

LiteTurn is a research tool for bicycle activity/gesture detection and road condition awareness. It uses Android accelerometer and gyroscope sensory information to detect the state of the user, as well as road conditions such as potholes and speed bumps. 

At HackTX 2014, we developed a prototype consumer product that used Myo gesture and orientation sensors to detect turn signal gestures, Android GPS Bearing information to detect end of turns, and a wireless Spark Core hooked up to a 24-Neopixel LED ring that acted as turn lights. These products were available at hand, but could be easily replaced by a consumer smartwatch and a cheap microcontroller + bluetooth chip to provide lower energy usage, lower costs, and more accurate recognition.

In the future, the hope is to develop LiteTurn into a system that can detect the following activities in real-time:

* Turn signal gestures
* Start and end of turn
* Pedaling
* Braking
* Dismounting

The end goal is to provide research insights into Real-time Pothole Detection on Two-Wheeled Transports.

There have been papers published on pothole detection using different algorithms to varying degrees of success in terms of distinguishing potholes from other natural road conditions, like speed bumps, manhole covers, and sudden elevations from hills and walkways ([Example](http://ieeexplore.ieee.org/xpl/articleDetails.jsp?reload=true&arnumber=5982206)). They have all focused on accelerometer readings taken from 4-wheeled vehicles. With reliable detection of cyclist activities, it should be possible to extract pothole readings from the much stronger and wider range of noise that would likely be received due to the physical nature of bicycling.
