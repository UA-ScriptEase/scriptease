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


int main(){

	list<StoryPoint>::iterator it;

	StoryPoint gen1("gen1", 1);
    StoryPoint gen2("gen2",1);
    StoryPoint gen3("gen3",1);
    StoryPoint AA("AA", 1);
    StoryPoint BB("BB", 2);
    StoryPoint CC("CC",2);


	StoryPoint::RegisterRoot("start",1);

	for(it=allPoints.begin(); it!=allPoints.end(); it++){
		cout << "1 The list of points includes " << it->uniqueName << endl;
	}

    allPoints.push_back(gen1);
	StoryPoint::RegisterChild("start", "gen1", 1);
	for(it=allPoints.begin(); it!=allPoints.end(); it++){
			cout << "2 The list of points includes " << it->uniqueName << endl;
	}

	//StoryPoint::RegisterChild("start", "gen2", 1);
	//StoryPoint::RegisterChild("start", "gen3", 1);
	StoryPoint::RegisterChild("gen1", "AA", 1);
	for(it=allPoints.begin(); it!=allPoints.end(); it++){
		cout << "3 The list of points includes " << it->uniqueName << endl;
	}
	StoryPoint::RegisterChild("gen2", "BB", 2);
	StoryPoint::RegisterChild("gen3", "BB", 2);
	StoryPoint::RegisterChild("gen1", "CC", 3);
	StoryPoint::RegisterChild("gen2", "CC", 2);
	StoryPoint::RegisterChild("gen3", "CC", 2);

	for(it=allPoints.begin(); it!=allPoints.end(); it++){
			cout << "4 The list of points includes " << it->uniqueName << endl;
	}

/*
	for(it=root.children.begin(); it!=root.children.end(); it++){
		cout << "Root's children " << it->uniqueName << endl;
	}
	for(it=gen1.children.begin(); it!=gen1.children.end(); it++){
			cout << "gen1's children " << it->uniqueName << endl;
		}
	for(it=gen2.children.begin(); it!=gen2.children.end(); it++){
				cout << "gen2's children " << it->uniqueName << endl;
			}
	for(it=gen3.children.begin(); it!=gen3.children.end(); it++){
				cout << "gen3's children " << it->uniqueName << endl;
			}

*/



	return 0;


}
