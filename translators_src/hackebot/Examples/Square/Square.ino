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
	void SucceedStoryPointAfterSecondsSeconds(string StoryPoint_0, float Seconds);
	void MoveDirectionAtSpeedSpeed(float Speed, int Direction);
	void Do(int subject);
	void TurnDirection(int Direction_0);
	int IfStoryPointIsActive_0(string StoryPoint_1);
	void ContinueAtStoryPointImmediately(string StoryPoint_2, string autoStoryPoint);
	void TurnRandomlyRightOrLeft();
	void WhenRobotIsWithinDistanceCentimetersFromAnObstacle(int subject_0, float Distance);
	int IfStoryPointIsActive_1(string StoryPoint_3);
	
	void StoryPointSetup(){
	    if(!storyInitialized) {
	        StoryPoint::RegisterRoot("start9", 1);
	        string parentName;
	        parentName = "start9";
	        StoryPoint::RegisterChild(parentName, "forward10", 1);
	        StoryPoint::RegisterChild(parentName, "detect14", 1);
	        parentName = "forward10";
	        StoryPoint::RegisterChild(parentName, "right11", 1);
	        parentName = "right11";
	        parentName = "detect14";
	        StoryPoint::SucceedStoryPoint("start9");
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
	    float Distance;
	    subject_0 = 1;
	    Distance = 5.0;
	    
	    Do(subject);
	    WhenRobotIsWithinDistanceCentimetersFromAnObstacle(subject_0, Distance);
	    
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
	void Do(int subject) {
	    string StoryPoint;
	    int IsActive;
	    int Question;
	    
	    StoryPoint = "forward10";
	    IsActive = IfStoryPointIsActive(StoryPoint);
	    Question = IsActive;
	    
	    if(Question){
	        float Speed;
	        int Direction;
	        string StoryPoint_0;
	        float Seconds;
	        Speed = 1.0;
	        Direction = 0;
	        StoryPoint_0 = "forward10";
	        Seconds = 1.0;
	        MoveDirectionAtSpeedSpeed(Speed, Direction);
	        SucceedStoryPointAfterSecondsSeconds(StoryPoint_0, Seconds);
	    } else {
	    }
	    
	    string StoryPoint_1;
	    int IsActive_0;
	    int Question_0;
	    
	    StoryPoint_1 = "right11";
	    IsActive_0 = IfStoryPointIsActive_0(StoryPoint_1);
	    Question_0 = IsActive_0;
	    
	    if(Question_0){
	        int Direction_0;
	        string StoryPoint_2;
	        string autoStoryPoint;
	        Direction_0 = 1;
	        StoryPoint_2 = "forward10";
	        autoStoryPoint = "right11";
	        TurnDirection(Direction_0);
	        ContinueAtStoryPointImmediately(StoryPoint_2, autoStoryPoint);
	    } else {
	    }
	    
	}
	void TurnDirection(int Direction_0) {
	    if(Direction_0 == 0){
	        hackebot.TurnL(10 , 450);
	    } else {
	        hackebot.TurnR(10, 450);
	    }
	    SE_GlobalTime = SE_GlobalTime + 450;
	}
	int IfStoryPointIsActive_0(string StoryPoint_1) {
	    return StoryPoint::CheckEnabled(StoryPoint_1);
	}
	void ContinueAtStoryPointImmediately(string StoryPoint_2, string autoStoryPoint) {
	    SE_GlobalTime = 0;
	    StoryPoint::ContinueAtStoryPoint(StoryPoint_2);
	}
	void TurnRandomlyRightOrLeft() {
	    int SE_RandTurn = random(200);
	    if(SE_RandTurn < 99)
	        hackebot.TurnR(10, 450);
	    else if(SE_RandTurn > 100)
	        hackebot.TurnL(10, 450);
	    SE_GlobalTime = SE_GlobalTime + 450;
	}
	void WhenRobotIsWithinDistanceCentimetersFromAnObstacle(int subject_0, float Distance) {
	    while(obstacle.Ping(Distance)){
	        string StoryPoint_3;
	        int IsActive_1;
	        int Question_1;
	        
	        StoryPoint_3 = "detect14";
	        IsActive_1 = IfStoryPointIsActive_1(StoryPoint_3);
	        Question_1 = IsActive_1;
	        
	        if(Question_1){
	            TurnRandomlyRightOrLeft();
	        } else {
	        }
	        
	    }
	}
	int IfStoryPointIsActive_1(string StoryPoint_3) {
	    return StoryPoint::CheckEnabled(StoryPoint_3);
	}
	