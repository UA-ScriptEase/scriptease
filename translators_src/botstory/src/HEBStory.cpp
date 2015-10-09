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
list<StoryPoint> storyTree;
bool storyInitialized = false;

/**
 * Story Point constructors
 */
StoryPoint::StoryPoint(){

	//Default constructor is for the root only since it is the only StoryPoint
	//we initialize automatically. Has no Parents.
	list<StoryPoint> children;
	this->fanIn = 0;
	this->uniqueName = "Null";
	this->state = DISABLED;
}

StoryPoint::StoryPoint(string uniqueName, int fanIn){

	list<StoryPoint> children, parent;
	this->fanIn = fanIn;
	this->uniqueName = uniqueName;
	this->state = DISABLED;

}

/*
 * Returns the current state of the given StoryPoint.
 */
bool StoryPoint::CheckEnabled(StoryPoint storyPoint){
	return storyPoint.state == ENABLED;
}

bool StoryPoint::CheckSucceeded(StoryPoint storyPoint){
	return storyPoint.state == SUCCEEDED;
}

bool StoryPoint::CheckFailed(StoryPoint storyPoint){
	return storyPoint.state == FAILED;
}
int StoryPoint::CheckState(string uniqueName){
	StoryPoint * storyPoint = FindStoryPoint(uniqueName);
	cout << "Inside checkstate" << endl;
	cout << "state of " << storyPoint->uniqueName << " is " << storyPoint->state << endl;
	if(storyPoint->state == ENABLED)
		return ENABLED;
	else if(storyPoint->state == SUCCEEDED)
		return SUCCEEDED;
	else if(storyPoint->state == FAILED)
		return FAILED;

	return 0;
}

/*
 * Enable the StoryPoint. If the StoryPoint was previously marked as succeeded,
 * we automatically succeed the point.
 */
void StoryPoint::EnableStoryPoint(){

	if(this->state == ENABLED)
		return;

	State previousState = this->state;
	this->state = ENABLED;

	if(previousState == PRESUCCEEDED)
		SucceedStoryPoint(this->uniqueName);
}
/*
 * Succeed the StoryPoint and enable any children that meet their fan in.
 * Sets the state to PRESUCCEEDED if the story point is not yet enabled.
 */
void StoryPoint::SucceedStoryPoint(string uniqueName){
	StoryPoint * storyPoint = FindStoryPoint(uniqueName);

	cout << "inside succeedstorypoint with " << storyPoint->uniqueName << " with state " << storyPoint->state << endl;
	list<StoryPoint>::iterator it, on;

	if(storyPoint != NULL){
		//If the State of the StoryPoint is PRE/SUCCEEDED then do nothing
		if(storyPoint->state == PRESUCCEEDED || storyPoint->state == SUCCEEDED) return;

		//If the StoryPoint's state is enabled we need to Succeed it and then enable all its children
		if(storyPoint->state == ENABLED){
			storyPoint->state = SUCCEEDED;

			for(it = storyPoint->children.begin(); it != storyPoint->children.end(); it++){
				cout << "inside succeed succeedstorypoint " << storyPoint->uniqueName << " children: " << it->uniqueName << endl;
			}

			//For the children of the StoryPoint
			for(it = storyPoint->children.begin(); it != storyPoint->children.end(); it++){

				cout << "what is the state of 'it' " << it->state << endl;
				if(it->state == ENABLED || it->state == SUCCEEDED) continue;

				cout << "what is 'it' pointing at ? " << it->uniqueName << endl;

				//Need to keep track of how many parents have been succeeded. We
				//can't enable a child if not all of it's parent's have been succeeded
				int succeededParents = 0;
				for(on = it->parent.begin(); on != it->parent.end(); on++){
					cout << "Did i get into parents list " << endl;
					cout << it->uniqueName << "'s parents are : " << on->uniqueName << endl;
					if(CheckSucceeded(*on)) succeededParents++;
				}

				if(succeededParents >= storyPoint->fanIn){
					storyPoint->EnableStoryPoint();
					break;
				}
			}
		} else if (storyPoint->state == DISABLED){
			storyPoint->state = PRESUCCEEDED;
		}
	} else {
		//cout << "Attempted to Succeed nonexistant StoryPoint " << uniqueName << endl;
	}
	cout << "end of succeed storyPoint " << storyPoint->uniqueName << " with state " << storyPoint->state << endl;
}

/*
 * Fails the StoryPoint and all of it's children.
 */
void StoryPoint::FailStoryPoint(string uniqueName){
	list<StoryPoint>::iterator it;
	StoryPoint * storyPoint = FindStoryPoint(uniqueName);

	if(storyPoint != NULL){
		for(it = storyPoint->children.begin(); it != storyPoint->children.end(); it++){
			it->state = FAILED;
		}
		storyPoint->state = FAILED;
	} else {
		//cout << "Attempted to fail nonexistant StoryPoint " << uniqueName << endl;
	}

}

/*
 * Continue at the specified StoryPoint. We need to disable any descendants it has
 * that might be active, and then enable it.
 */
void StoryPoint::ContinueAtStoryPoint(string uniqueName){
	StoryPoint * storyPoint = FindStoryPoint(uniqueName);
	if(storyPoint != NULL){
		storyPoint->DisableDescendants();
		storyPoint->EnableStoryPoint();
	}
}


/*
 * Finds the given StoryPoint in the StoryTree.
 */
StoryPoint * StoryPoint::FindStoryPoint(string uniqueName){
	StoryPoint * ptr = 0;

	//cout << "Inside find story point looking for: " << uniqueName << endl;

	list<StoryPoint>::iterator it;
	for(it = storyTree.begin(); it != storyTree.end(); it++){
		if(it->uniqueName == uniqueName){
			ptr = &(*it);
			return ptr;
		}
		else {
			//cout << "could not find the storypoint in the tree: " << uniqueName << endl;
		}
	}

	cout << endl;
	return NULL;
}

/*
 * Get the descendants of a StoryPoint object (ie: "this"). Uses a recursive call
 * to iterate through each child, and then all of their children. Different from
 * just a StoryPoint's children.
 *
 * root-->A-->B-->C
 *	\	  \-->D
 *   \
 *    \---->E
 *
 *   EX: Root's Descendants are ABCDE
 *   	 A's descendants are BCD but A's children would be BD
 *   	 E Has none
 */
list<StoryPoint> StoryPoint::GetDescendants(){
	list<StoryPoint> descendants;
	list<StoryPoint> recursiveDesc;
	list<StoryPoint>::iterator it;
	list<StoryPoint>::iterator on;

	for(it = this->children.begin(); it != this->children.end(); it++){
		descendants.push_back(*it);
		recursiveDesc=it->GetDescendants();
		for(on = recursiveDesc.begin(); on != recursiveDesc.end(); on++){
			descendants.push_back(*on);
		}
	}

	return descendants;
}

/*
 * Disable the descendants of a story point. Uses a recursive call to disable all the
 * children's children of the beginning StoryPoint.
 */
void StoryPoint::DisableDescendants(){
	list<StoryPoint>::iterator it;
	for(it = this->children.begin(); it != this->children.end(); it++){
		it->state = DISABLED;
		it->DisableDescendants();
	}
}

/*
 * Get all active Story Points. Not really used anywhere but hey functionality.
 */
list<string> GetAllActive(){
	list<string> active;
	list<StoryPoint>::iterator it;
	list<StoryPoint> descendants = root.GetDescendants();

	for(it = storyTree.begin();it != storyTree.end(); it++){
		//if(CheckEnabled(*it)){
			active.push_back(it->uniqueName);
		//}
	}
	return active;
}

/*
 * Register the root of the story. Should only be called once since any story should only
 * one root. This is called in the Language Dictionary and is automatically generated.
 * Initializes the storyInitialized variable so we're only initializing the root once
 */
void StoryPoint::RegisterRoot(string uniqueName, int fanIn){
	root.uniqueName = uniqueName;
	root.fanIn = fanIn;
	storyInitialized = true;
	root.EnableStoryPoint();
	storyTree.push_back(root);
}

/*
 * Registers a StoryPoint as a child to the parent. Children can be shared between parents
 * so checks to see if the given Child has already been created. If it hasn't, then creates
 * a new StoryPoint, adds it to the StoryTree and then adds it to the given parent. If it
 * does exist, then just adds it to the given parent.
 */
void StoryPoint::RegisterChild(string parentName, string uniqueName, int fanIn){
	list<StoryPoint>::iterator it;
	StoryPoint * parent = FindStoryPoint(parentName);

	if(parent != NULL){
		StoryPoint * child = FindStoryPoint(uniqueName);

		if(child == NULL){
			StoryPoint newChild = StoryPoint(uniqueName, fanIn);
			storyTree.push_back(newChild);

			StoryPoint * newPoint = FindStoryPoint(uniqueName);
			parent->AddChild(newPoint);
		}

		if(child != NULL){
			parent->AddChild(child);
		}
	}
	else {
		cout << "Could not find parent with unique name " << parentName << endl;
	}
}

/*
 * Add a StoryPoint to the list. "This" being references is the parent we are
 * attaching the child to.
 */
void StoryPoint::AddChild(StoryPoint * child){
	this->children.push_back(*child);
	child->parent.push_back(*this);
}

/*
 * We want to know the details of a story point. Checks parents and children
 */

void StoryPoint::CheckDetails(StoryPoint sp){
	list<StoryPoint>::iterator it, on;

	cout << "Inside checkdetails with " << sp.uniqueName << endl;
	for(on = sp.parent.begin(); on != sp.parent.end(); it++){
		cout << on->uniqueName << "'s parent(s): " << it->uniqueName << endl;
	}


	for(on = sp.children.begin(); on != sp.children.end(); it++){
			cout << on->uniqueName << "'s child(ren): " << it->uniqueName << endl;
	}

}

void StoryPoint::CheckAllDetails(list<StoryPoint> pointlist){
	list<StoryPoint>::iterator it, on;

	cout << endl;
	cout << "Inside check all details " << endl;
	for(it = pointlist.begin(); it != pointlist.end(); it++){

		cout << "Print out storyPoint name " << it->uniqueName << endl;
		for(on = it->parent.begin(); on != it->parent.end(); it++){
			cout << it->uniqueName << "'s parent(s): " << on->uniqueName << endl;
		}


		for(on = it->children.begin(); on != it->children.end(); it++){
				cout << it->uniqueName << "'s child(ren): " << on->uniqueName << endl;
		}
	}

}




