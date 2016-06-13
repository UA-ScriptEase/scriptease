// This will allow the easy get feedback from the Sonar sensor for the Hack-E-Bot.
// 
// Written by Richard Albritton for Hack-E-Bot Robotics, BSD license

#if ARDUINO >= 100
 #include "Arduino.h"
#else
 #include "WProgram.h"
#endif

class HackEBot_Sonar {
  public:
    HackEBot_Sonar(int T, int E);
    boolean Ping(int D); //-- to send a sonar ping, D = how far away an object has to be before ping returns TRUE.
	long microsecondsToCentimeters(long M); //-- figures out how far a returned ping has travelled in CM, M = microseconds
  private:
   int sonarTrig;
   int sonarEcho;
   int Distance;
   int duration;
   int cm;
};
