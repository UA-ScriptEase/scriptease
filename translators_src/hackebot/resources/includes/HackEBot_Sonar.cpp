/*
  Sonar.cpp - Library for moving the Hack-E-Bot.
  Created by Richard Albritton, June 9, 2014.
  Released into the public domain.
*/

#if ARDUINO >= 100
 #include "Arduino.h"
#else
 #include "WProgram.h"
#endif

#include "HackEBot_Sonar.h"

/*
 * From the Hack-e-bot github: https://github.com/Hack-E-Bot/HackEBot
 *
 */

HackEBot_Sonar::HackEBot_Sonar(int T, int E)
{
  sonarTrig = T;
  sonarEcho = E;
  pinMode(T, OUTPUT); // Sends the chirp (Blue)
  pinMode(E, INPUT); // Listens for the chirp (Green)
  
}

boolean HackEBot_Sonar::Ping(int D){  //-- to send a sonar ping, D = how far away an object has to be before Obstacle = TRUE.
  Distance = D;
  /* The PING))) is triggered by a HIGH pulse of 2 or more microseconds.
  Give a short LOW pulse beforehand to ensure a clean HIGH pulse:*/
  digitalWrite(sonarTrig, LOW);
  delayMicroseconds(2);
  digitalWrite(sonarTrig, HIGH);
  delayMicroseconds(5);
  digitalWrite(sonarTrig, LOW);
  /* The sonarEcho pin is used to read the signal from the PING))): a HIGH
  pulse whose duration is the time (in microseconds) from the sending
  of the ping to the reception of its echo off of an object.*/
  duration = pulseIn(sonarEcho, HIGH);
  
  // convert the time into a distance.

  cm = microsecondsToCentimeters(duration);
 
 
 if ( cm < Distance ) {
    return true;
  } else {
    return false;
  }
}

long HackEBot_Sonar::microsecondsToCentimeters(long M){ //-- figures out how far a returned ping has travelled in CM, M = microseconds
  // The speed of sound is 340 m/s or 29 microseconds per centimeter.
  // The ping travels out and back, so to find the distance of the
  // object we take half of the distance travelled.
  return M / 29 / 2;
}
