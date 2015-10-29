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
int main() {

	enum State {
		PRESUCCEEDED, SUCCEEDED, FAILED, ENABLED, DISABLED
	};

	StoryPoint::RegisterRoot("start2", 1);
	string parentName;
	parentName = "start2";
	StoryPoint::RegisterChild(parentName, "a3", 1);
	parentName = "a3";
	StoryPoint::RegisterChild(parentName, "b4", 1);
	StoryPoint::RegisterChild(parentName, "c5", 1);
	parentName = "b4";
	StoryPoint::RegisterChild(parentName, "d6", 1);
	parentName = "d6";
	parentName = "c5";
	StoryPoint::RegisterChild(parentName, "d6", 1);
	StoryPoint::SucceedStoryPoint("start2");
	cout << " " << endl;
	StoryPoint::SucceedStoryPoint("a3");
	cout << " " << endl;
	StoryPoint::SucceedStoryPoint("b4");
	cout << " " << endl;
	StoryPoint::SucceedStoryPoint("c5");
	cout << " " << endl;
	StoryPoint::SucceedStoryPoint("d6");
	cout << " " << endl;

	cout << "continue check " << endl;
	StoryPoint::ContinueAtStoryPoint("b4");
	StoryPoint::CheckState("a3");
	StoryPoint::CheckState("b4");
	StoryPoint::CheckState("c5");
	StoryPoint::CheckState("d6");




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

