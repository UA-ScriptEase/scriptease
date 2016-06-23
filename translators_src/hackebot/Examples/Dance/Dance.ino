#ifdef __AVR_ATtiny85__
	    #include <avr/power.h>
	#endif
	#include <StandardCplusplus.h>
	#include <system_configuration.h>
	#include <unwind-cxx.h>
	#include <utility.h>
	#include <Servo.h>
	#include <HEBStory.h>
	#include <HackEBot_Move.h>
	#include <HackEBot_Sonar.h>
	
	int SE_GlobalTime;
	
	Servo RightS;
	Servo LeftS;
	HackEBot_Move hackebot(5,6);
	HackEBot_Sonar obstacle(3,4);
	
	int IfStoryPointIsActive(string StoryPoint);
	void Do(int subject);
	void SucceedStoryPointAfterSecondsSeconds(string StoryPoint_0, float Seconds);
	void MoveDirectionAtSpeedSpeed(float Speed, int Direction);
	void TurnDegreesDegreesDirection(float Degrees, int Direction_0);
	void ContinueAtStoryPointImmediately(string StoryPoint_1, string autoStoryPoint);
	int IfStoryPointIsActive_0(string StoryPoint_2);
	void MoveDirectionAtSpeedSpeed_0(float Speed_0, int Direction_1);
	int IfStoryPointIsActive_1(string StoryPoint_3);
	void SucceedStoryPointAfterSecondsSeconds_0(string StoryPoint_4, float Seconds_0);
	int IfStoryPointIsActive_2(string StoryPoint_5);
	void Do_0(int subject_0);
	void AdjustWheelWheelByNumber(int Wheel, float Number);
	void SucceedStoryPointImmediately(string StoryPoint_6);
	
	void StoryPointSetup(){
	    if(!storyInitialized) {
	        StoryPoint::RegisterRoot("start23", 1);
	        string parentName;
	        parentName = "start23";
	        StoryPoint::RegisterChild(parentName, "adjustwheels27", 1);
	        StoryPoint::RegisterChild(parentName, "forward24", 1);
	        parentName = "adjustwheels27";
	        parentName = "forward24";
	        StoryPoint::RegisterChild(parentName, "back25", 1);
	        parentName = "back25";
	        StoryPoint::RegisterChild(parentName, "spin26", 1);
	        parentName = "spin26";
	        StoryPoint::SucceedStoryPoint("start23");
	    }
	}
	
	void setup() { 
	    #if defined (__AVR_ATtiny85__) && (F_CPU == 16000000L)
	    clock_prescale_set(clock_div_1);
	    #endif
	    
	    RightS.attach(5);
	    LeftS.attach(6);
	    pinMode(3, OUTPUT);
	    pinMode(4, INPUT);
	    SE_GlobalTime = 0;
	    
	    StoryPointSetup();
	    
	}
	
	void loop() {
	    int subject;
	    subject = 1;
	    int subject_0;
	    subject_0 = 1;
	    
	    Do(subject);
	    Do_0(subject_0);
	    
	}
	
	int IfStoryPointIsActive(string StoryPoint) {
	    return StoryPoint::CheckEnabled(StoryPoint);
	}
	void Do(int subject) {
	    string StoryPoint;
	    int IsActive;
	    int Question;
	    
	    StoryPoint = "back25";
	    IsActive = IfStoryPointIsActive(StoryPoint);
	    Question = IsActive;
	    
	    if(Question){
	        float Speed;
	        int Direction;
	        string StoryPoint_0;
	        float Seconds;
	        Speed = 1.0;
	        Direction = 1;
	        StoryPoint_0 = "back25";
	        Seconds = 2.0;
	        MoveDirectionAtSpeedSpeed(Speed, Direction);
	        SucceedStoryPointAfterSecondsSeconds(StoryPoint_0, Seconds);
	    } else {
	    }
	    
	    string StoryPoint_2;
	    int IsActive_0;
	    int Question_0;
	    
	    StoryPoint_2 = "spin26";
	    IsActive_0 = IfStoryPointIsActive_0(StoryPoint_2);
	    Question_0 = IsActive_0;
	    
	    if(Question_0){
	        float Degrees;
	        int Direction_0;
	        string StoryPoint_1;
	        string autoStoryPoint;
	        Degrees = 450.0;
	        Direction_0 = 0;
	        StoryPoint_1 = "forward24";
	        autoStoryPoint = "spin26";
	        TurnDegreesDegreesDirection(Degrees, Direction_0);
	        ContinueAtStoryPointImmediately(StoryPoint_1, autoStoryPoint);
	    } else {
	    }
	    
	    string StoryPoint_3;
	    int IsActive_1;
	    int Question_1;
	    
	    StoryPoint_3 = "forward24";
	    IsActive_1 = IfStoryPointIsActive_1(StoryPoint_3);
	    Question_1 = IsActive_1;
	    
	    if(Question_1){
	        float Speed_0;
	        int Direction_1;
	        string StoryPoint_4;
	        float Seconds_0;
	        Speed_0 = 1.0;
	        Direction_1 = 0;
	        StoryPoint_4 = "forward24";
	        Seconds_0 = 2.0;
	        MoveDirectionAtSpeedSpeed_0(Speed_0, Direction_1);
	        SucceedStoryPointAfterSecondsSeconds_0(StoryPoint_4, Seconds_0);
	    } else {
	    }
	    
	}
	void SucceedStoryPointAfterSecondsSeconds(string StoryPoint_0, float Seconds) {
	    if(SE_GlobalTime >= ((int)Seconds* 1000 )){
	        StoryPoint::SucceedStoryPoint(StoryPoint_0);
	        SE_GlobalTime = 0;
	    }
	}
	void MoveDirectionAtSpeedSpeed(float Speed, int Direction) {
	    int SE_Speed = Speed;
	    if(SE_Speed > 80){
	        SE_Speed = 80;
	    } else if (SE_Speed < 0) {
	        SE_Speed = 0;
	    }
	    if(Direction == 0){
	        hackebot.MoveF((SE_Speed+ 10.5) , 250);
	    } else { 
	        hackebot.MoveB((SE_Speed + 10.5), 250); 
	    }
	    SE_GlobalTime = SE_GlobalTime + 250;
	}
	void TurnDegreesDegreesDirection(float Degrees, int Direction_0) {
	    int SE_TimePerDegree = Degrees* 5;
	    if(Direction_0 == 0){
	        hackebot.TurnL(10, SE_TimePerDegree);
	    } else {
	        hackebot.TurnR(10, SE_TimePerDegree);
	    }
	}
	void ContinueAtStoryPointImmediately(string StoryPoint_1, string autoStoryPoint) {
	    SE_GlobalTime = 0;
	    StoryPoint::ContinueAtStoryPoint(StoryPoint_1);
	}
	int IfStoryPointIsActive_0(string StoryPoint_2) {
	    return StoryPoint::CheckEnabled(StoryPoint_2);
	}
	void MoveDirectionAtSpeedSpeed_0(float Speed_0, int Direction_1) {
	    int SE_Speed = Speed_0;
	    if(SE_Speed > 80){
	        SE_Speed = 80;
	    } else if (SE_Speed < 0) {
	        SE_Speed = 0;
	    }
	    if(Direction_1 == 0){
	        hackebot.MoveF((SE_Speed+ 10.5) , 250);
	    } else { 
	        hackebot.MoveB((SE_Speed + 10.5), 250); 
	    }
	    SE_GlobalTime = SE_GlobalTime + 250;
	}
	int IfStoryPointIsActive_1(string StoryPoint_3) {
	    return StoryPoint::CheckEnabled(StoryPoint_3);
	}
	void SucceedStoryPointAfterSecondsSeconds_0(string StoryPoint_4, float Seconds_0) {
	    if(SE_GlobalTime >= ((int)Seconds_0* 1000 )){
	        StoryPoint::SucceedStoryPoint(StoryPoint_4);
	        SE_GlobalTime = 0;
	    }
	}
	int IfStoryPointIsActive_2(string StoryPoint_5) {
	    return StoryPoint::CheckEnabled(StoryPoint_5);
	}
	void Do_0(int subject_0) {
	    string StoryPoint_5;
	    int IsActive_2;
	    int Question_2;
	    
	    StoryPoint_5 = "adjustwheels27";
	    IsActive_2 = IfStoryPointIsActive_2(StoryPoint_5);
	    Question_2 = IsActive_2;
	    
	    if(Question_2){
	        int Wheel;
	        float Number;
	        string StoryPoint_6;
	        Wheel = 1;
	        Number = -2.0;
	        StoryPoint_6 = "adjustwheels27";
	        AdjustWheelWheelByNumber(Wheel, Number);
	        SucceedStoryPointImmediately(StoryPoint_6);
	    } else {
	    }
	    
	}
	void AdjustWheelWheelByNumber(int Wheel, float Number) {
	    if(Wheel== 0){
	        hackebot.AdjustRight(Number);
	    } else {
	        hackebot.AdjustLeft(Number);
	    }
	}
	void SucceedStoryPointImmediately(string StoryPoint_6) {
	    StoryPoint::SucceedStoryPoint(StoryPoint_6);
	}
	