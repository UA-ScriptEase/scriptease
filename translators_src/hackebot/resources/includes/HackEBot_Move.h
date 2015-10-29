// This will allow the easy control of the two servos for the Hack-E-Bot.
// 
// Written by Richard Albritton for Hack-E-Bot Robotics, BSD license

#if ARDUINO >= 100
 #include "Arduino.h"
#else
 #include "WProgram.h"
#endif

extern Servo RightS;
extern Servo LeftS;


class HackEBot_Move {
  public:
    HackEBot_Move(int L, int R);
    long ServoSetup(int D, int F, int G, int H, int J, int K); //-- used to manually set the servo limits
    void Calibrate(); //-- to Calibrate the servos
	void MoveStop(int S); //-- to stop, S = repeat number.
    void MoveF(int S, int Z); //-- to drive forward, S = Speed, Z = Time.
    void MoveB(int S, int Z); //-- to drive backward, S = Speed, Z = Time.
    void TurnR(int S, int Z); //-- to turn right, S = Speed, Z = Time.
    void TurnL(int S, int Z); //-- to turn left, S = Speed, Z = Time.

  private:
   int servoL; 
   int servoR; 
   int CenterL; 
   int CenterR; 


};
