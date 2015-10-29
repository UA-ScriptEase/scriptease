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

class StoryPoint {

public:

	enum State {PRESUCCEEDED, SUCCEEDED, FAILED, ENABLED, DISABLED};


	list<StoryPoint> children;
	list<StoryPoint> parent;
	int fanIn;
	string uniqueName;
	State state;

	StoryPoint();									// Default constructor
	StoryPoint(string uniqueName, int fanIn);   	// Constructor

	static bool CheckSucceeded(string storyPoint);
	static bool CheckEnabled(string storyPoint);
	static bool CheckFailed(string storyPoint);

	void AddChild(StoryPoint * child);
	void DisableDescendants();
	void ContinueAt();
	bool Exists();

	list<StoryPoint> GetDescendants();
	static list<string> GetAllActive();
	static StoryPoint * FindStoryPoint(string uniqueName);

	void EnableStoryPoint();
	static void SucceedStoryPoint(string uniqueName);
	static void FailStoryPoint(string uniqueName);
	static void ContinueAtStoryPoint(string uniqueName);
	static void RegisterRoot(string uniqueName, int fanIn);
	static void RegisterChild(string parentName, string uniqueName, int fanIn);

	static void CheckDetails(StoryPoint sp);
	static void CheckAllDetails(list<StoryPoint> st);

};

extern StoryPoint root;
extern list<StoryPoint> storyTree;
extern bool storyInitialized;

#endif /* HEBSTORY_H_ */
