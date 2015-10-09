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
static void PrintList(list<StoryPoint> stlist);
int main(){

	enum State {PRESUCCEEDED, SUCCEEDED, FAILED, ENABLED, DISABLED};

	StoryPoint::RegisterRoot("start1", 1);
	string parentName;
	parentName = "start1";
	StoryPoint::RegisterChild(parentName, "a2", 1);
	parentName = "a2";
/*
	StoryPoint::RegisterChild(parentName, "b3", 1);
	parentName = "b3";
	//StoryPoint::SucceedStoryPoint("start1");

	cout << endl;
	StoryPoint * sp = StoryPoint::FindStoryPoint("a2");

	list<StoryPoint>::iterator it;
	cout << "Checking the parents of : " << sp->uniqueName << endl;
	for(it = sp->parent.begin(); it != sp->parent.end(); it++){
		cout << it->uniqueName << endl;
	}
	cout << endl;

	if(StoryPoint::CheckState("a2") == ENABLED){
		cout << "a2 is enabled" << endl;
	}
*/

	//StoryPoint::CheckAllDetails(storyTree);

	PrintStoryTree();
	return 0;


}
void PrintList(list<StoryPoint> stlist){
	list<StoryPoint>::iterator it;
	cout << "Checking the parents: " << endl;
	for(it = stlist.begin(); it != stlist.end(); it++){
		cout << it->uniqueName << endl;
	}
}

void PrintStoryTree(){
	list<StoryPoint>::iterator it, on;

	cout << "The current list of story points is: " << endl;
	for(it = storyTree.begin(); it != storyTree.end(); it++){
		cout << it->uniqueName << endl;
		cout << "trying to print children of " << it->uniqueName << endl;
		for(on = it->children.begin(); on != it->children.end(); on++){
			cout << on->uniqueName << endl;
		}
		cout << "trying to print parent of " << it->uniqueName << endl;
		for(on = it->parent.begin(); on != it->parent.end(); on++){
			cout << on->uniqueName << endl;
			cout << endl;
		}
	}
	cout << endl;
}


