/*
 * main.cpp
 *
 *  Created on: Aug 19, 2015
 *      Author: scriptease
 */

#include <iostream>
#include <iterator>
#include <list>
#include "HEBStory.h"

using namespace std;

static void PrintStoryTree();
void CheckStates();

void PrintTime(int time);

int SE_GlobalTime;

void DoForSecondsSecondsAndThenSucceed(float Seconds, string StoryPoint_0) {
  if (StoryPoint::CheckEnabled(StoryPoint_0)) {
    if (SE_GlobalTime <= ((int) Seconds * 1000)) {
      SE_GlobalTime += 250;
    } else {
      cout << "Move Forward Before: " << SE_GlobalTime << endl;
      SE_GlobalTime = 0;
      StoryPoint::SucceedStoryPoint(StoryPoint_0);
      cout << "Move Forward After: " << SE_GlobalTime << endl;
    }
  }
}

void DoForSecondsSecondsAndThenSucceed_0(float Seconds_0, string StoryPoint_2) {
  if (StoryPoint::CheckEnabled(StoryPoint_2)) {
    if (SE_GlobalTime <= ((int) Seconds_0 * 1000)) {
      SE_GlobalTime += 250;
    } else {
      cout << "Move Back Before: " << SE_GlobalTime << endl;
      SE_GlobalTime = 0;
      StoryPoint::ContinueAtStoryPoint("a3");
      cout << "Move Back After: " << SE_GlobalTime << endl;
    }
  }
}


int main() {

	SE_GlobalTime =0;
	int i = 0;

	enum State {
		PRESUCCEEDED, SUCCEEDED, FAILED, ENABLED, DISABLED
	};


	StoryPoint::RegisterRoot("start2", 1);
	string parentName;
	parentName = "start2";
	StoryPoint::RegisterChild(parentName, "a3", 1);
	parentName = "a3";
	StoryPoint::RegisterChild(parentName, "b4", 1);
	parentName = "b4";
	StoryPoint::RegisterChild(parentName, "c5", 1);
	StoryPoint::SucceedStoryPoint("start2");

	while(i != 8){
		DoForSecondsSecondsAndThenSucceed_0(2.0, "a3");
		DoForSecondsSecondsAndThenSucceed_0(2.0, "b4");
		cout << "Global Time in main loop: " << SE_GlobalTime << endl;
		i++;
	}

	CheckStates();
	cout << "Succeed A at B" << endl;
	StoryPoint::SucceedStoryPoint("a3");
	CheckStates();
	cout << "Continue at A disable B" << endl;
	StoryPoint::ContinueAtStoryPoint("a3");
	CheckStates();

	cout << endl;

	return 0;

}


void CheckStates() {
	list<StoryPoint>::iterator it, on;
	for (it = storyTree.begin(); it != storyTree.end(); it++) {
		cout << "State of " << it->uniqueName << " is " << it->state << endl;
	}

}

void PrintStoryTree() {
	list<StoryPoint>::iterator it, on;

	for (it = storyTree.begin(); it != storyTree.end(); it++) {
		cout << "The current list of story points is: " << endl;
		cout << it->uniqueName << endl;
		cout << "trying to print children of " << it->uniqueName << endl;
		for (on = it->children.begin(); on != it->children.end(); on++) {
			cout << on->uniqueName << endl;
		}
		cout << "trying to print parent of " << it->uniqueName << endl;
		for (on = it->parent.begin(); on != it->parent.end(); on++) {
			cout << on->uniqueName << endl;
			cout << endl;
		}
		cout << " " << endl;
	}
}

