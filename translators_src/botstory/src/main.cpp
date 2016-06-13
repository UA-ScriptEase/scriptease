/*
 * main.cpp
 *
 *  Created on: Aug 19, 2015
 *      Author: ehill
 *
 *  This is a test environment for Hack-E-Bot so that you can examine the C++
 *  code in a dedicated IDE or terminal environment rather than the Arduino IDE
 *  and it's serial monitor. Replicates all the instructions that would be sent
 *  to the robot within Arduino.
 *
 *  To use:
 *
 *  1) Generate your code in ScriptEase and open in Arduino, copy the contents
 *  of the 'Do' loop and place it in the for loop (you'll need to delete the underscore
 *  and number from the end of the method calls)
 *
 *  2) Copy the 'StoryPointSetup" method. Ignore 'if(!storyInitialized)'
 *
 *  3) If using the "when object is within distance" cause in ScriptEase copy and paste
 *  the "WhenRobotIsWithinDistanceCentimetersFromAnObstacle" from Arduino to the same method
 *  here.
 *
 *
 */

#include <iostream>
#include <iterator>
#include <list>
#include "HEBStory.h"

using namespace std;

static void PrintStoryTree();
void CheckStates(int round);
int SE_GlobalTime;

void ContinueAtStoryPointImmediately(string StoryPoint_8,
		string autoStoryPoint_0) {
	StoryPoint::ContinueAtStoryPoint(StoryPoint_8);
	SE_GlobalTime = 0;
	cout << "\tContinuing at " << StoryPoint_8 << endl;
	cout << endl;

}
void ContinueAtStoryPointAfterSecondsSeconds(string StoryPoint, float Seconds) {
	if (SE_GlobalTime >= ((int) Seconds * 1000)) {
		StoryPoint::ContinueAtStoryPoint(StoryPoint);
		SE_GlobalTime = 0;
		cout << "\tContinuing at " << StoryPoint << endl;
		cout << endl;

	}
}

void SucceedStoryPointImmediately(string StoryPoint_7) {
	StoryPoint::SucceedStoryPoint(StoryPoint_7);
	SE_GlobalTime = 0;
	cout << "\tSucceeding " << StoryPoint_7 << endl;
	cout << endl;

}

void SucceedStoryPointAfterSecondsSeconds(string StoryPoint_1,
		float Seconds_0) {
	if (SE_GlobalTime >= ((int) Seconds_0 * 1000)) {
		StoryPoint::SucceedStoryPoint(StoryPoint_1);
		SE_GlobalTime = 0;
		cout << "\tSucceeding " << StoryPoint_1 << endl;
		cout << endl;

	}
}

int IfStoryPointIsActive(string StoryPoint_0) {
	return StoryPoint::CheckEnabled(StoryPoint_0);
}

void MoveDirectionAtSpeedSpeed(float Speed, int Direction) {

	if (Direction == 0)
		cout << "\tForward ";
	else
		cout << "\tBackward ";

	SE_GlobalTime = SE_GlobalTime + 250;
	cout << "\t" << SE_GlobalTime << endl;

}

void TurnRandomlyRightOrLeft() {
	SE_GlobalTime = SE_GlobalTime + 250;
	cout << "\tRandom\t" << SE_GlobalTime << endl;

}

void TurnDirection(int Direction_0) {
	if (Direction_0 == 0)
		cout << "\tLeft \t";
	else
		cout << "\tRight \t";

	SE_GlobalTime = SE_GlobalTime + 450;
	cout << "\t" << SE_GlobalTime << endl;

}

void TurnDegreesDegreesDirection(float Degrees, int Direction_3) {
	int SE_TimePerDegree = Degrees * 5;
	if (Direction_3 == 0) {
		cout << "\tLeft by \t " << Degrees;
	} else {
		cout << "\tRight by \t" << Degrees;

	}
	SE_GlobalTime += SE_TimePerDegree;
}

void AdjustWheelWheelByNumber(int Wheel, float Number) {

	cout << "\tAdjust \t" << SE_GlobalTime << endl;

}

void DelayTheRobot() {
	SE_GlobalTime = SE_GlobalTime + 250;
	cout << "\tDelaying\t" << SE_GlobalTime << endl;

}

void WhenRobotIsWithinDistanceCentimetersFromAnObstacle(float Distance) {
	string StoryPoint_9;
	int IsActive_4;
	int Question_4;

	StoryPoint_9 = "detect41";
	IsActive_4 = IfStoryPointIsActive(StoryPoint_9);
	Question_4 = IsActive_4;

	if (Question_4) {
		TurnRandomlyRightOrLeft();
	} else {
	}

}

void Detect(float r) {
	cout << "     Detecting at" << r << endl;
}

int main() {

	SE_GlobalTime = 0;

	enum State {
		PRESUCCEEDED, SUCCEEDED, FAILED, ENABLED, DISABLED
	};

	/**
	 * start -> forward -> back -> left -> right (return to forward)
	 * 		 -> adjust -> detect (stay at detect)
	 *
	 *  Copy the "StoryPointSetup" method from Arduino here
	 *
	 */
	StoryPoint::RegisterRoot("start35", 1);
	string parentName;
	parentName = "start35";
	StoryPoint::RegisterChild(parentName, "forward36", 1);
	StoryPoint::RegisterChild(parentName, "adjust40", 1);
	parentName = "forward36";
	StoryPoint::RegisterChild(parentName, "back37", 1);
	parentName = "back37";
	StoryPoint::RegisterChild(parentName, "left38", 1);
	parentName = "left38";
	StoryPoint::RegisterChild(parentName, "right39", 1);
	parentName = "right39";
	parentName = "adjust40";
	StoryPoint::RegisterChild(parentName, "detect41", 1);
	parentName = "detect41";
	StoryPoint::SucceedStoryPoint("start35");

	PrintStoryTree();
	CheckStates(0);

	//place the contents of the Arduino 'Do' loop in here.
	for (int i = 1; i <= 50; i++) {

		string StoryPoint;
		int IsActive;
		int Question;

		StoryPoint = "forward36";
		IsActive = IfStoryPointIsActive(StoryPoint);
		Question = IsActive;

		if (Question) {
			float Speed;
			int Direction;
			string StoryPoint_0;
			float Seconds;
			Speed = 1.0;
			Direction = 0;
			StoryPoint_0 = "forward36";
			Seconds = 1.0;
			MoveDirectionAtSpeedSpeed(Speed, Direction);
			SucceedStoryPointAfterSecondsSeconds(StoryPoint_0, Seconds);
		} else {
		}

		string StoryPoint_1;
		int IsActive_0;
		int Question_0;

		StoryPoint_1 = "back37";
		IsActive_0 = IfStoryPointIsActive(StoryPoint_1);
		Question_0 = IsActive_0;

		if (Question_0) {
			float Speed_0;
			int Direction_0;
			string StoryPoint_2;
			float Seconds_0;
			Speed_0 = 1.0;
			Direction_0 = 1;
			StoryPoint_2 = "back37";
			Seconds_0 = 1.0;
			MoveDirectionAtSpeedSpeed(Speed_0, Direction_0);
			SucceedStoryPointAfterSecondsSeconds(StoryPoint_2, Seconds_0);
		} else {
		}

		string StoryPoint_3;
		int IsActive_1;
		int Question_1;

		StoryPoint_3 = "left38";
		IsActive_1 = IfStoryPointIsActive(StoryPoint_3);
		Question_1 = IsActive_1;

		if (Question_1) {
			int Direction_1;
			string StoryPoint_4;
			float Seconds_1;
			Direction_1 = 0;
			StoryPoint_4 = "left38";
			Seconds_1 = 1.0;
			TurnDirection(Direction_1);
			SucceedStoryPointAfterSecondsSeconds(StoryPoint_4, Seconds_1);
		} else {
		}

		string StoryPoint_5;
		int IsActive_2;
		int Question_2;

		StoryPoint_5 = "right39";
		IsActive_2 = IfStoryPointIsActive(StoryPoint_5);
		Question_2 = IsActive_2;

		if (Question_2) {
			int Direction_2;
			string StoryPoint_6;
			float Seconds_2;
			Direction_2 = 1;
			StoryPoint_6 = "forward36";
			Seconds_2 = 1.0;
			TurnDirection(Direction_2);
			ContinueAtStoryPointAfterSecondsSeconds(StoryPoint_6, Seconds_2);
		} else {
		}

		string StoryPoint_8;
		int IsActive_3;
		int Question_3;

		StoryPoint_8 = "adjust40";
		IsActive_3 = IfStoryPointIsActive(StoryPoint_8);
		Question_3 = IsActive_3;

		if (Question_3) {
			int Wheel;
			float Number;
			string StoryPoint_7;
			Wheel = 1;
			Number = 0.0;
			StoryPoint_7 = "adjust40";
			AdjustWheelWheelByNumber(Wheel, Number);
			SucceedStoryPointImmediately(StoryPoint_7);
		} else {
		}



		//WhenRobotIsWithinDistanceCentimetersFromAnObstacle(5.0);

		//CheckStates(i);
	}

	CheckStates(999);

	return 0;

}

/**
 * Checks the state of the entire StoryPoint tree
 */
void CheckStates(int round) {
	cout << endl;

	list<StoryPoint>::iterator it, on;
	cout << "States ";
	if (round == 0)
		cout << "at the beginning:" << endl;
	else if (round == 999)
		cout << "at the end:" << endl;
	else
		cout << "after round " << round << ":" << endl;

	for (it = storyTree.begin(); it != storyTree.end(); it++) {
		cout << "\t" << it->uniqueName << " is ";

		if (it->state == 0)
			cout << "\tpre-succeeded" << endl;
		if (it->state == 1)
			cout << "\tSucceeded" << endl;
		if (it->state == 2)
			cout << "\tFailed" << endl;
		if (it->state == 3)
			cout << "\tEnabled" << endl;
		if (it->state == 4)
			cout << "\tDisabled" << endl;
	}
	cout << endl;

}

void PrintStoryTree() {
	list<StoryPoint>::iterator it, on;

	cout << "The current list of story points is:      " << endl;
	for (it = storyTree.begin(); it != storyTree.end(); it++) {
		cout << it->uniqueName << " ";
	}
	cout << endl;

	cout << endl;
	for (it = storyTree.begin(); it != storyTree.end(); it++) {
		cout << it->uniqueName << endl;
		cout << "    children: ";
		for (on = it->children.begin(); on != it->children.end(); on++) {
			cout << on->uniqueName << " ";
		}
		cout << endl;
		cout << "    parent of " << it->uniqueName << ": ";
		for (on = it->parent.begin(); on != it->parent.end(); on++) {
			cout << on->uniqueName << " ";
		}
		cout << endl;
		cout << endl;

	}
	cout << endl;
}

