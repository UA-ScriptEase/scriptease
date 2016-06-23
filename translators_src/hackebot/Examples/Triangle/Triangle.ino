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
	
	void Do(int subject);
	void MoveDirectionAtSpeedSpeed(float Speed, int Direction);
	int IfStoryPointIsActive(string StoryPoint);
	void SucceedStoryPointAfterSecondsSeconds(string StoryPoint_0, float Seconds);
	int IfStoryPointIsActive_0(string StoryPoint_1);
	void ContinueAtStoryPointImmediately(string StoryPoint_2, string autoStoryPoint);
	void TurnDegreesDegreesDirection(float Degrees, int Direction_0);
	
	void StoryPointSetup(){
	    if(!storyInitialized) {
	        StoryPoint::RegisterRoot("start15", 1);
	        string parentName;
	        parentName = "start15";
	        StoryPoint::RegisterChild(parentName, "forward16", 1);
	        parentName = "forward16";
	        StoryPoint::RegisterChild(parentName, "turn60derees17", 1);
	        parentName = "turn60derees17";
	        StoryPoint::SucceedStoryPoint("start15");
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
	    
	    Do(subject);
	    
	}
	
	void Do(int subject) {
	    string StoryPoint;
	    int IsActive;
	    int Question;
	    
	    StoryPoint = "forward16";
	    IsActive = IfStoryPointIsActive(StoryPoint);
	    Question = IsActive;
	    
	    if(Question){
	        float Speed;
	        int Direction;
	        string StoryPoint_0;
	        float Seconds;
	        Speed = 1.0;
	        Direction = 0;
	        StoryPoint_0 = "forward16";
	        Seconds = 1.0;
	        MoveDirectionAtSpeedSpeed(Speed, Direction);
	        SucceedStoryPointAfterSecondsSeconds(StoryPoint_0, Seconds);
	    } else {
	    }
	    
	    string StoryPoint_1;
	    int IsActive_0;
	    int Question_0;
	    
	    StoryPoint_1 = "turn60derees17";
	    IsActive_0 = IfStoryPointIsActive_0(StoryPoint_1);
	    Question_0 = IsActive_0;
	    
	    if(Question_0){
	        float Degrees;
	        int Direction_0;
	        string StoryPoint_2;
	        string autoStoryPoint;
	        Degrees = 60.0;
	        Direction_0 = 1;
	        StoryPoint_2 = "forward16";
	        autoStoryPoint = "turn60derees17";
	        TurnDegreesDegreesDirection(Degrees, Direction_0);
	        ContinueAtStoryPointImmediately(StoryPoint_2, autoStoryPoint);
	    } else {
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
	int IfStoryPointIsActive(string StoryPoint) {
	    return StoryPoint::CheckEnabled(StoryPoint);
	}
	void SucceedStoryPointAfterSecondsSeconds(string StoryPoint_0, float Seconds) {
	    if(SE_GlobalTime >= ((int)Seconds* 1000 )){
	        StoryPoint::SucceedStoryPoint(StoryPoint_0);
	        SE_GlobalTime = 0;
	    }
	}
	int IfStoryPointIsActive_0(string StoryPoint_1) {
	    return StoryPoint::CheckEnabled(StoryPoint_1);
	}
	void ContinueAtStoryPointImmediately(string StoryPoint_2, string autoStoryPoint) {
	    SE_GlobalTime = 0;
	    StoryPoint::ContinueAtStoryPoint(StoryPoint_2);
	}
	void TurnDegreesDegreesDirection(float Degrees, int Direction_0) {
	    int SE_TimePerDegree = Degrees* 5;
	    if(Direction_0 == 0){
	        hackebot.TurnL(10, SE_TimePerDegree);
	    } else {
	        hackebot.TurnR(10, SE_TimePerDegree);
	    }
	}
	