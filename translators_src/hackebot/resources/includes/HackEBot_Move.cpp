/*
  HackEBot.cpp - Library for moving the Hack-E-Bot.
  Created by Richard Albritton, June 9, 2014.
  Released into the public domain.
*/

#include <Servo.h>
#include "HackEBot_Move.h"


/**
 * Added AdjustRight and AdjustLeft for easier
 */
// Number in cm when the rover will reverse and try to navigate around.
const int obstacle = 8;

int left, right;
// Duration of a ping, distance in inches, distance converted to cm.
long duration, inches, cm;

HackEBot_Move::HackEBot_Move(int L, int R){
	left = 90;
	right = 90;
	servoR = L;  // Right Servo control line (orange) on Pin #13
	servoL = R;   // Left Servo control line (orange) on Pin #8
	CenterR = right;  //This sets the center or Stopping point for the servo. If the robot drifts to one side, try changing this number by +/- 1.
	CenterL = left;  //This sets the center or Stopping point for the servo. If the robot drifts to one side, try changing this number by +/- 1.

}
/* The Hack-E-Bot can now be programmed to move by giving it instructions like:
MoveStop(stopTime); -- to stop moving
MoveF(moveSpeed, moveTime); -- to drive forward
MoveB(moveSpeed, moveTime); -- to drive backward
TurnR(moveSpeed, moveTime); -- to turn right
TurnL(moveSpeed, moveTime); -- to turn left

stopTime and moveTime are measured in milliseconds.
moveSpeed is a number between 0 to 90 with 0 being no movement and 90 being full speed.
*/
 
void HackEBot_Move::MoveStop(int T){ // Stop moving, T = stopTime.
  RightS.write(CenterR);
  LeftS.write(CenterL);
  delay(T);
}
 
void HackEBot_Move::MoveF(int S, int T){ // Move forward, S = moveSpeed, T = moveTime.
  RightS.write(CenterR - S);
  LeftS.write(S + CenterL);
  delay(T);
}
 
void HackEBot_Move::MoveB(int S, int T){ // Move backward, S = moveSpeed, T = moveTime.
  RightS.write(S + CenterR);
  LeftS.write(CenterL - S);
  delay(T);
}
 
void HackEBot_Move::TurnL(int S, int T){ // Turn Left, S = moveSpeed, T = moveTime.
  if(S < 8)
	  S = 8;
  else if(S > 90)
	  S = 90;

  RightS.write(CenterR - S);
  LeftS.write(CenterL - S);
  delay(T);
}
 
void HackEBot_Move::TurnR(int S, int T){ // Turn Right, S = moveSpeed, T = moveTime.
  if(S < 8)
	S = 8;
  else if(S > 90)
	S = 90;

  RightS.write(S + right);
  LeftS.write(S + left);
  delay(T);
}


//This could be done nicer. Bad smell!
void HackEBot_Move::AdjustRight(int r){
	right += ((int) r);
	CenterR = right;
}

void HackEBot_Move::AdjustLeft(int l){
	left += ((int) l);
	CenterL = left;
}

 
