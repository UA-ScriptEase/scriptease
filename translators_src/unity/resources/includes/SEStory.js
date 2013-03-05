#pragma strict

/*
 * This class defines functions used by ScriptEase II to implement the story
 * system. They probably shouldn't be used in your own scripts if you are
 * developing primarily with ScriptEase, as it may break the story system.
 *
 * Author: ScriptEase II Team 
 */

static var root:StoryPoint;

//static var CurrentStoryPoint : String;

/* 
 * Registers the root of the story. This should only be called when we first
 * start it.
 */
static function RegisterRoot(uniqueName:String, fanIn:int) {
	root = new StoryPoint(uniqueName, fanIn);
}

static function RegisterChildren(parentName:string, uniqueName:String fanIn:int) {


}

static function FindStoryPoint(uniqueName:String) {
	
}

// Represents a Story Point from ScriptEase II.
class StoryPoint {
	var children : List.<StoryPoint> = new List.<StoryPoint>();
	var fanIn : int;
	var uniqueName : String;

	var state : State = State.DISABLED;

	enum State {
		SUCCEEDED,
		FAILED,
		ENABLED,
		DISABLED,
	}

	function StoryPoint(uniqueName : String, fanIn : int) {
		this.uniqueName = uniqueName;
		this.fanIn = fanIn;
	}
	
	function Enable() {
		this.state = State.ENABLED;
	}
	
	function Succeed() {
		this.state = State.SUCCEEDED;
	}
	
	function Fail() {
		this.state = State.FAILED;
	}
	
	function IsEnabled() {
		return this.state == State.ENABLED;
	}
	
	function HasSucceeded() {
		return this.state == State.SUCCEEDED;
	}
	
	function HasFailed() {
		return this.state == State.FAILED;
	}
	
	// Add a parent to the StoryPoint
	function AddParent(parent:StoryPoint) {
		this.parents.Add(parent);
	}
	
	// Add a list of parents to the StoryPoint
	function AddParents(parents:List.<StoryPoint>) {
		for(parent in parents)
			this.parents.Add(parent);
	}
}