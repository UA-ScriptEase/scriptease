#pragma strict
import System.Collections.Generic;

/**
 * This class defines functions used by ScriptEase II to implement the story
 * system. They probably shouldn't be used in your own scripts if you are
 * developing primarily with ScriptEase, as it may break the story system.
 *
 * @author ScriptEase II Team 
 */

static var root:StoryPoint;

/**
 * Registers the root of the story. This should only be called when we first
 * start it.
 */
static function RegisterRoot(uniqueName:String, fanIn:int) {
	root = new StoryPoint(uniqueName, fanIn);
}

static function RegisterChild(parentName:String, uniqueName:String, fanIn:int) {
	var parent:StoryPoint = FindStoryPoint(parentName);
	
	if(parent != null) {
		var child:StoryPoint = FindStoryPoint(uniqueName);
		
		if(child != null) {
			parent.AddChild(child);
		} else 
			parent.AddChild(new StoryPoint(uniqueName, fanIn));
	} else {
		Debug.Log("SESTory Warning: Could not find parent with unique name "
			+ uniqueName);
	}
}

/**
 * Succeeds the passed in Story Point and all of its parents. Enables the
 * Story Points after it if their fan in is met.
 */
static function SucceedStoryPoint(uniqueName:String) {
	var storyPoint:StoryPoint = FindStoryPoint(uniqueName);
	
	if(storyPoint != null) {
		storyPoint.Succeed();
	} else
		Debug.Log("SEStory Warning: Attempted to succeed nonexistant Story " +
		"Point " + uniqueName);
}

/**
 * Fails the passed in Story Point and all of its children.
 */
static function FailStoryPoint(uniqueName:String) {
	var storyPoint:StoryPoint = FindStoryPoint(uniqueName);
	
	if(storyPoint != null) {
		storyPoint.Fail();
	} else
		Debug.Log("SEStory Warning: Attempted to fail nonexistant Story Point "
			+ uniqueName);
}

/**
 * Continues the story at the Story Point, setting it to enabled and all of its
 * children to disabled.
 */
static function ContinueAtStoryPoint(uniqueName:String) {
	
}

static function HasSucceeded(uniqueName:String):boolean {
	var storyPoint:StoryPoint = FindStoryPoint(uniqueName);
	
	if(storyPoint != null)
		return storyPoint.HasSucceeded();
	else {
		Debug.Log("SEStory Warning: Attempted to find succeeded state of " +
			"nonexistant Story Point " + uniqueName);
		return false;
	}

}

static function IsEnabled(uniqueName:String):boolean  {
	var storyPoint:StoryPoint = FindStoryPoint(uniqueName);
	
	if(storyPoint != null)
		return storyPoint.IsEnabled();
	else {
		Debug.Log("SEStory Warning: Attempted to find enabled state of " +
			"nonexistant Story Point " + uniqueName);
		return false;
	}
}

static function HasFailed(uniqueName:String):boolean  {
	var storyPoint:StoryPoint = FindStoryPoint(uniqueName);
	
	if(storyPoint != null)
		return storyPoint.HasFailed();
	else {
		Debug.Log("SEStory Warning: Attempted to find failed state of " +
			"nonexistant Story Point " + uniqueName);
		return false;
	}
}

/**
 * Finds the Story Point that matches the unique name.
 */
private static function FindStoryPoint(uniqueName:String):StoryPoint {
	if(root.uniqueName == uniqueName) 
		return root;
	else {
		for(child in root.children) {
			if(child.uniqueName == uniqueName)
				return child;
			else
				return FindStoryPoint(child.uniqueName);		
		}
	}
	
	Debug.Log("SEStory Warning: Could not find Story Point with unique name "
		+ uniqueName);
		
	return null;
}

/**
 * Represents a Story Point from ScriptEase II.
 */
private class StoryPoint {
	var children:List.<StoryPoint>;
	var parents:List.<StoryPoint>;
	var fanIn:int;
	var uniqueName:String;
	var state : State;
	
	enum State {
		PRESUCCEEDED,
		SUCCEEDED,
		FAILED,
		ENABLED,
		DISABLED,
	}

	function StoryPoint(uniqueName:String, fanIn:int) {
		this.uniqueName = uniqueName;
		this.fanIn = fanIn;
		this.children = new List.<StoryPoint>();
		this.parents = new List.<StoryPoint>();
		this.state = State.DISABLED;
	}
	
	function Enable() {
		this.state = State.ENABLED;
		
		if(this.state == State.PRESUCCEEDED)
			this.Succeed();	
	}
	
	/**
	 * Succeed the Story Point and enable any children that meet their fan in.
	 * Sets the state to State.PRESUCCEEDED if the story point is not yet
	 * enabled.
	 */
	function Succeed() {
		if(this.state == State.SUCCEEDED || this.state == State.PRESUCCEEDED)
			return;
		
		if(this.state == State.ENABLED) {
			this.state = State.SUCCEEDED;
		
			for(child in this.children) {
				if(child.state == State.ENABLED || child.state == State.SUCCEEDED)
					continue; 
			
				var succeededParents:int = 0;
			
				for(parent in this.parents) {
					if(parent.HasSucceeded())
						succeededParents++;		
				}
			
				if(succeededParents >= child.fanIn) {
					child.Enable();
				}
			}	
		} else if (this.state == State.DISABLED) {
			this.state = State.PRESUCCEEDED;
		}		
	}
	
	/**
	 * Sets the Story Point's state to failed and fails all of its children.
	 * This will automatically fail all descendants of the first Story Point
	 * that is failed.
	 */
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
	
	// Add a child to the StoryPoint
	function AddChild(child:StoryPoint) {
		this.children.Add(child);
		child.parents.Add(this);
	}







	// MAY BE ABLE TO DELETE THESE
	
	/**
	 * Returns all ancestors of the Story Point, not including the Story Point 
	 * itself.
	 */
	function GetAncestors():List.<StoryPoint> {
		var ancestors:List.<StoryPoint> = new List.<StoryPoint>();
		
		if(this.uniqueName != SEStory.root.uniqueName) {
			ancestors.Add(SEStory.root);
		
			for(child in SEStory.root.children) {
				if(child.GetDescendants().Contains(this))
					ancestors.Add(child);
			}	
		}
		
		return ancestors;
	}
	
	/**
	 * Returns all descendants of the Story Point, including the Story Point itself.
	 */
	function GetDescendants():List.<StoryPoint> {
		var descendants:List.<StoryPoint> = new List.<StoryPoint>();
		
		descendants.Add(this);
		
		for(child in children) {
			for(descendant in child.GetDescendants()) {
				if(!descendants.Contains(descendant))
					descendants.Add(descendant);
			}
		}
		
		return descendants;
	}
}
