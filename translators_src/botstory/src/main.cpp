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

/*

	StoryPoint gen1("gen1", 1);
    StoryPoint gen2("gen2",1);
    StoryPoint gen3("gen3",1);
    StoryPoint AA("AA", 1);
    StoryPoint BB("BB", 2);
    StoryPoint CC("CC",2);

*/


	StoryPoint::RegisterRoot("start",1);
	//PrintStoryTree();

	StoryPoint::RegisterChild("start", "gen1", 1);
	StoryPoint::RegisterChild("start", "gen2", 1);
	StoryPoint::RegisterChild("start", "gen3", 1);
	//PrintStoryTree();

	StoryPoint::RegisterChild("gen1", "AA", 1);
	StoryPoint::RegisterChild("gen2", "BB", 2);
	StoryPoint::RegisterChild("gen3", "BB", 2);
	//PrintStoryTree();

	StoryPoint::RegisterChild("gen1", "CC", 3);
	StoryPoint::RegisterChild("gen2", "CC", 2);
	StoryPoint::RegisterChild("gen3", "CC", 2);
	//PrintStoryTree();


	list<StoryPoint> stlist = StoryPoint::GetParents("start");
	//PrintList(stlist);


	//StoryPoint::CheckAllDetails(storyTree);

	return 0;


}
void PrintList(list<StoryPoint> stlist){
	list<StoryPoint>::iterator it;
	cout << "Checking the parents: " << endl;
	for(it = stlist.begin(); it != stlist.end(); it++){
		cout << it->uniqueName << endl;
	}
}
/*
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
*/

