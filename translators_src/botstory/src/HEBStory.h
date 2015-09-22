/*
 * HEBStory.h
 *
 *  Created on: May 5, 2015
 *      Author: ehill
 */

#ifndef HEBSTORY_H_
#define HEBSTORY_H_

#include <string>
#include <list>

using namespace std;
static bool storyInitialized = false;

class StoryPoint {

public:

	enum State {PRESUCCEEDED, SUCCEEDED, FAILED, ENABLED, DISABLED, EXISTS, NONEXIST};


	list<StoryPoint> children;
	list<StoryPoint> parent;
	int fanIn;
	string uniqueName;
	State state, state2;

	StoryPoint();									// Default constructor
	StoryPoint(string uniqueName, int fanIn);   	// Constructor

	bool CheckEnabled();
	static bool IsEnabled(string uniqueName);
	void Enabled();

	bool CheckSucceeded();
	static bool HasSucceeded(string uniqueName);
	void Succeed();

	bool CheckFailed();
	static bool HasFailed(string uniqueName);
	void Fail();

	void AddChild(StoryPoint child);
	void DisableDescendants();
	void ContinueAt();
	bool Exists();

	list<StoryPoint> GetDescendants();
	static list<string> GetAllActive();
	static StoryPoint FindStoryPoint(string uniqueName);

	static void SucceedStoryPoint(string uniqueName);
	static void FailStoryPoint(string uniqueName);
	static void ContinueAtStoryPoint(string uniqueName);

	static void RegisterRoot(string uniqueName, int fanIn);
	static void RegisterChild(string parentName, string uniqueName, int fanIn);

	static void CheckDetails(StoryPoint currentPoint);

};

extern StoryPoint root;
extern StoryPoint nullPoint;
extern list<StoryPoint> allPoints;

#endif /* HEBSTORY_H_ */
