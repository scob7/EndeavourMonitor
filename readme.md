Endeavour Monitor Readme
========================

The following instructions are install a custom digital monitoring system for use in marine or other mobile vechile setups. 
A raspberry pi is used to run the server and database to poll sensors wired to numerous locations in the vechicle such as engine, exhaust, fridge, and freezer.

Safety Disclaimer
=================
Modifying your vehicle to install the sensors and wire the power is done completely at your own risk. Any information provided here is for reference only. 
Improper installation of wiring and sensors could result in serious damage to your vehicle and put you in serious danger. 
Please consult the appropriate marine or automotive professionals if you are unsure or need assistance.


Physical Setup
==============

The raspberry pi needs to be wired for power, sensors, and a real-time clock to function. A power supply capable of converting from your source (12V DC in our case)
to 5V DC is required for power. A length of shielded 3 wire (power, ground, and signal) is needed to connect the sensors in a bus style configuration. Finally, a 
real-time clock (aka RTC) is needed if the raspberry is to be used when no internet access is avaible, which is typical for mobile setups. The RTC is used to ensure
the raspberry pi is using accurate date and time, as turning off and on the device with no internet access will default the clock back to epoch in 1969 causing issues
with data logging.

Next, figure out where you want to place your sensors. The system is designed for 5-10 to displaying nicely on the dashboard. Routing the wires to you desired 
sensor locations can be difficult. You most likely will need to drill holes so please take care not to damage existing systems. Avoid running the wires in locations
that may experience moisture or extreme temperatures that may damage the wires over time.

Software Setup
==============

Install Postgresql database. Run .sql file to create tables.

Install Java. Copy or build jar file. Run jar as service.

Configure unregistered sensors.
- Touch sensor to determine which is which
- Provide a label
- Colors are automatic

Basic features
- Date ranges
- Data scrolling
- Turn on or off plot lines

Network and Security
====================

The dashboard is available to anyone on the same network as the raspberry pi. 
In our setup we used a mobile hot spot device (TP-LINK model here) which is capable of WISP (local wifi + wifi as isp). 
The raspberry pi is wired to the portable device and a local private wifi network is created to connect our tablets/computers
which need to access the dashboard. Then if wifi internet is available you can configure the hot-spot device connect and enable
all devices on the private wifi to also access the internet. If you instead choose to use existing wifi networks be advised that all
other users would be able to view your dashboard. If you require usernames and passwords it could be added in a future release.

  

