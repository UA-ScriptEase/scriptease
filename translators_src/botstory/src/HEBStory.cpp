#include <iostream>
#include <sstream>
#include <iterator>
#include "HEBStory.h"

using namespace std;

/*
 *
 *	This class is used by ScriptEase to implement the Story system with
 *	the Hack-E-Bot robots.
 *
 *	@author ehill
 *
 */

StoryPoint root;
StoryPoint nullPoint;
list<StoryPoint> allPoints;

/**
 * Story Point constructors
 */
StoryPoint::StoryPoint(){

	//Default constructor is for the root and nullpoint only since it is the only StoryPoint
	//we initialize automatically. Has no Parents.
	list<StoryPoint> children;
	this->fanIn = 0;
	this->uniqueName = "Null";
	this->state = DISABLED;
	this->state2 = NONEXIST;
}

StoryPoint::StoryPoint(string uniqueName, int fanIn){

	list<StoryPoint> children;
	list<StoryPoint> parent;
	this->fanIn = fanIn;
	this->uniqueName = uniqueName;
	this->state = DISABLED;
	this->state2 = EXISTS;

}

/**
 * Recursively searches for a Story Point in the descendants of the passed in point.
 */

StoryPoint FindStoryPointInDescendants(StoryPoint parent, string childName){
	list<StoryPoint>::iterator it;

	if(parent.uniqueName == childName){
		return parent;
	}
	else{
		for(it=parent.children.begin(); it != parent.children.end(); it++){
				cout << parent.uniqueName <<" {list of children} " << it->uniqueName <<endl;

			}
		for(it=parent.children.begin(); it != parent.children.end(); it++){
			StoryPoint child = *it;
			StoryPoint foundPoint = FindStoryPointInDescendants(child, childName);
			if(foundPoint.Exists()){
				return foundPoint;
			}
		}
	}
	return nullPoint;
}

/**
 * Finds the Story Point that matches the unique name.
 */

StoryPoint StoryPoint::FindStoryPoint(string uniqueName){
	StoryPoint storyPoint = FindStoryPointInDescendants(root, uniqueName);
	return storyPoint;
}


/**
 * Enables the Story Point. If the Story Point was marked as succeeded,
 * we automatically succeed the point.
 */
void StoryPoint::Enabled(){
 	if(this->state == ENABLED)
 		return;

 	State previousState = this->state;

 	this->state2 = EXISTS;
 	this->state = ENABLED;

 	if(previousState == PRESUCCEEDED)
 		this->Succeed();

}

/**
 * Succeed the Story Point and enable any children that meet their fan in.
 * Sets the state to State.PRESUCCEEDED if the story point is not yet
 * enabled.
 */

void StoryPoint::Succeed(){
	list<StoryPoint>::iterator it;
	list<StoryPoint>::iterator on;
	if(this->state == SUCCEEDED || this->state == PRESUCCEEDED)
		return;

	if(this->state == ENABLED)
	{
		this->state = SUCCEEDED;

		//for the children of this story point
		for(it= this->children.begin(); it != this->children.end(); it++){
			if(it->state == ENABLED || it->state == SUCCEEDED)
				continue;

			int succeededParents = 0;

			//for the parents of this child
			for(on = it->parent.begin(); on != this->parent.end(); on++){
				if(on->CheckSucceeded())
					succeededParents++;
				}
				if(succeededParents >= this->fanIn){
					this->Enabled();
					break;
				}
			}
	} else if (this->state == DISABLED){
			this->state = PRESUCCEEDED;
	}
}

/**
 * Returns all story points descendant from this one
 */

list<StoryPoint> StoryPoint::GetDescendants(){
	list<StoryPoint> descendants;
	list<StoryPoint> recursiveDesc;
	list<StoryPoint>::iterator it;
	list<StoryPoint>::iterator on;

	//Iterate through all the children of the story point we want the
	//descendants of. Then iterate through their children
	for(it = this->children.begin(); it != this->children.end(); it++){
		descendants.push_back(*it);
		recursiveDesc=it->GetDescendants();
		for(on = recursiveDesc.begin(); on != recursiveDesc.end(); on++){
			descendants.push_back(*on);
		}
	}
	return descendants;
}

/**
 * Enables the story point and disables all of its descendants
 */

void StoryPoint::ContinueAt() {
	this->DisableDescendants();
	this->Enabled();
}

/**
 * Disables all descendants of the story point.
 */

void StoryPoint::DisableDescendants(){
	list<StoryPoint>::iterator it;
	for(it = this->children.begin(); it != this->children.end(); it++){
		it->state = DISABLED;
		it->DisableDescendants();
	}
}

/**
 * Sets the Story Point's state to failed and fails all children.
 * Automatically fail all descendants of the first story point that is
 * failed
 */

void StoryPoint::Fail(){
	this->state = FAILED;
}

bool StoryPoint::CheckEnabled(){
	return this->state == ENABLED;
}

bool StoryPoint::CheckSucceeded(){
	return this->state == SUCCEEDED;
}

bool StoryPoint::CheckFailed(){
	return this->state == FAILED;
}

bool StoryPoint::Exists(){
	return this->state2 == EXISTS;
}


/**
 * Add a child to a Story Point, automatically adding the Story Point to
 * the child's parents.
 */

void StoryPoint::AddChild(StoryPoint child){
	cout << " " << endl;
	cout << "In AddChild" << endl;
	allPoints.push_back(child);
	this->children.push_back(child);
	list<StoryPoint>::iterator it;
	for(it=this->children.begin(); it != this->children.end(); it++){
		cout << this->uniqueName << " (list of children) " << it->uniqueName <<endl;

	}
	child.parent.push_back(*this);
	for(it=child.parent.begin(); it != child.parent.end(); it++){
			cout << child.uniqueName << " [list of parents] " << it->uniqueName <<endl;

	}
	CheckDetails(child);
}


/**
 * Register root of story. Should only be called once and happens in the Language Dictionary
 *
 */

void StoryPoint::RegisterRoot(string uniqueName, int fanIn){
	root.uniqueName = uniqueName;
	root.fanIn = fanIn;
	storyInitialized = true;
	root.Enabled();

	allPoints.push_back(root);

}

/**
 * Registers child node to a parent. Is called from the Language dictionary
 *
 */

void StoryPoint::RegisterChild(string parentName, string uniqueName, int fanIn){
	//need to check if there is a storypoint to begin with
	list<StoryPoint>::iterator it;
	for(it=allPoints.begin(); it!=allPoints.end(); it++){
		if(it->uniqueName == uniqueName){
			cout << "Found Story Point " << uniqueName << endl;
		}
		else{
			StoryPoint newPoint = StoryPoint(uniqueName, fanIn);
			allPoints.push_back(newPoint);
		}
	}
	for(it=allPoints.begin(); it!=allPoints.end(); it++){
		cout << it->uniqueName << endl;
	}





	/*	cout << "INSIDE REGISTER CHILD " << endl;
	cout << "" << endl;
	cout << flush;
	StoryPoint parent = FindStoryPoint(parentName);
	list<StoryPoint>::iterator it;

	if(parent.Exists()){
		StoryPoint child = FindStoryPoint(uniqueName);

		if(child.uniqueName == nullPoint.uniqueName){
			StoryPoint child = StoryPoint(uniqueName, fanIn);
			parent.AddChild(child);
			CheckDetails(child);
			for(it=parent.children.begin(); it != parent.children.end(); it++){
					//cout << parent.uniqueName << " :list of children: " << it->uniqueName <<endl;
					//EEDIT this only seems to get to the n-1 children. ie start has
					//gen1, gen2, gen3, will only write gen1, gen2
				cout << "Am i getting into this when there is only one child" << endl;
				CheckDetails(parent);

			}
		} else {
			for(it=parent.children.begin(); it != parent.children.end(); it++){
				cout << parent.uniqueName << " /list of children/ " << it->uniqueName <<endl;

			}
			parent.AddChild(child);
		}
	} else {
		cout << "Could not find parent with unique name " << parentName << endl;
	}*/

}

/**
 * Succeeds the passed in Story Point and all of its parents. Enables the Story Point
 * after it if their fan in is met.
 *
 */

void StoryPoint::SucceedStoryPoint(string uniqueName){
	StoryPoint storyPoint = FindStoryPoint(uniqueName);

	if(storyPoint.Exists()){
		storyPoint.Succeed();
	} else {
		//cout << "Attempted to Succeed nonexistant Story Point " << uniqueName << endl;
	}
}

/**
 * Fails the passed in Story Point and all of its children
 *
 */

void StoryPoint::FailStoryPoint(string uniqueName){
	StoryPoint storyPoint = FindStoryPoint(uniqueName);

	if(storyPoint.Exists()){
		storyPoint.Fail();
	} else {
		//cout << "Attempted to fail nonexistant Story Point " << uniqueName << endl;
	}
}

/**
 * Continues the story at the Story Point, setting it to enabled and all of its
 * children to disabled.
 */

void StoryPoint::ContinueAtStoryPoint(string uniqueName){
	StoryPoint storyPoint = FindStoryPoint(uniqueName);

	if(storyPoint.Exists()){
		storyPoint.ContinueAt();
	} else {
		//cout << "Attempted to continue at nonexistant Story Point " << uniqueName << endl;
	}
}

bool StoryPoint::HasSucceeded(string uniqueName){
	StoryPoint storyPoint = FindStoryPoint(uniqueName);

	if(storyPoint.Exists())
		return storyPoint.CheckSucceeded();
	else {
		//cout << "Attempted to find succeeded state of nonexistant Story Point " << uniqueName << endl;
		return false;
	}
	return false;
}

bool StoryPoint::IsEnabled(string uniqueName){
	StoryPoint storyPoint = FindStoryPoint(uniqueName);

	if(storyPoint.Exists()){
		return storyPoint.CheckEnabled();
	} else {
		//cout << "Attempted to find enabled state of Story Point " << uniqueName << endl;
		return false;
	}
	return false;
}

bool StoryPoint::HasFailed(string uniqueName){
	StoryPoint storyPoint = FindStoryPoint(uniqueName);

	if(storyPoint.Exists()){
		return storyPoint.CheckFailed();
	} else {
		//cout << "Attempted to find failed state of nonexistant Story Point" << uniqueName << endl;
		return false;
	}
	return false;
}

list<string> GetAllActive(){
	list<string> active;
	list<StoryPoint>::iterator it;
	list<StoryPoint> descendants = root.GetDescendants();

	if(root.CheckEnabled())
		active.push_back(root.uniqueName);

	for(it = descendants.begin(); it != descendants.end(); it++){
		if(it->CheckEnabled()){
			active.push_back(it->uniqueName);
		}
	}
	return active;
}

void StoryPoint::CheckDetails(StoryPoint currentPoint){
	//want to give a story point and find out exactly what it holds
	cout << "INSIDE CHECK DETAILS" << endl;
	list<StoryPoint>::iterator it;

	for(it=currentPoint.parent.begin(); it!=currentPoint.parent.end(); it++){
		//print out the parents of the current story point
		cout << currentPoint.uniqueName << "'s parent(s): " << it->uniqueName << endl;
	}
	for(it=currentPoint.children.begin(); it!=currentPoint.children.end(); it++){
			//print out the children of the current story point
		cout << currentPoint.uniqueName << "'s child(ren): " << it->uniqueName << endl;
	}

}








