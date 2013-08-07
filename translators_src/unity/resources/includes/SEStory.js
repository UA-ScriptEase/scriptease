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

static var storyInitialized:boolean = false;



/**
 * Registers the root of the story. This should only be called when we first
 * start it.
 */
static function RegisterRoot(uniqueName:String, fanIn:int) {
	root = new StoryPoint(uniqueName, fanIn);
	root.Enable();
	
	storyInitialized = true;
}

/**
 * Registers a child node to the parent.
 */
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
 * Adds a function to the Story Point that will be called when it succeeds.
 */
static function AddSucceedFunctionToStoryPoint(uniqueName:String, funxion:Function) {
	var storyPoint:StoryPoint = FindStoryPoint(uniqueName);
	
	if(storyPoint != null) {
		storyPoint.AddSucceedFunction(funxion);
	} else
		Debug.Log("SEStory Warning: Attempted to add succeed function to nonexistant Story " +
		"Point " + uniqueName);
}

/**
 * Adds a function to the Story Point that will be called when it fails.
 */
static function AddFailFunctionToStoryPoint(uniqueName:String, funxion:Function) {
	var storyPoint:StoryPoint = FindStoryPoint(uniqueName);
	
	if(storyPoint != null) {
		storyPoint.AddFailFunction(funxion);
	} else
		Debug.Log("SEStory Warning: Attempted to add fail function to nonexistant Story " +
		"Point " + uniqueName);
}

/**
 * Adds a function to the Story Point that will be called when it is enabled.
 */
static function AddEnableFunctionToStoryPoint(uniqueName:String, funxion:Function) {
	var storyPoint:StoryPoint = FindStoryPoint(uniqueName);
	
	if(storyPoint != null) {
		storyPoint.AddEnableFunction(funxion);
	} else
		Debug.Log("SEStory Warning: Attempted to add enable function to nonexistant Story " +
		"Point " + uniqueName);
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
	var storyPoint:StoryPoint = FindStoryPoint(uniqueName);
	
	if(storyPoint != null) {
		storyPoint.ContinueAt();
	} else
		Debug.Log("SEStory Warning: Attempted to continue at nonexistant " + 
			"Story Point " + uniqueName);
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

static function GetAllActive():List.<String> {
	var active:List.<String> = new List.<String>();
	
	if(root.IsEnabled())
		active.Add(root.uniqueName);
		
	for(descendant in root.GetDescendants()) {
		if(descendant.IsEnabled()) {
			active.Add(descendant.uniqueName);	
		}
	}
	
	return active;
}


/**
 * Finds the Story Point that matches the unique name.
 */
private static function FindStoryPoint(uniqueName:String):StoryPoint {
	var storyPoint = FindStoryPointInDescendants(root, uniqueName);
	
	return storyPoint;
}

/**
 * Recursively searches for a Story Point in the descendants of the passed in point.
 * This should only be called by FindStoryPoint(String).
 */
private static function FindStoryPointInDescendants(parent:StoryPoint, uniqueName:String):StoryPoint {
	if(parent.uniqueName == uniqueName) 
		return parent;
	else {
		for(child in parent.children) {
			var foundPoint:StoryPoint = FindStoryPointInDescendants(child, uniqueName);
				
			if(foundPoint != null)
				return foundPoint;
		}
	}
	
	return null;

}

/**
 * Represents a Story Point from ScriptEase II.
 */
private class StoryPoint {
	var enableFunctions : List.<Function>;
	var failFunctions : List.<Function>;
	var succeedFunctions : List.<Function>;

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

	/*
	 * Constructs a new StoryPoint based on the passed in unique name and
	 * fanIn. Also automatically sets the StoryPoint's state to DISABLED.
	 */
	function StoryPoint(uniqueName:String, fanIn:int) {
		this.uniqueName = uniqueName;
		this.fanIn = fanIn;
		this.children = new List.<StoryPoint>();
		this.parents = new List.<StoryPoint>();
		this.state = State.DISABLED;
		this.enableFunctions = new List.<Function>();
		this.succeedFunctions = new List.<Function>();
		this.failFunctions = new List.<Function>();
	}
	
	function AddEnableFunction(funxion : Function) {
		if(this.IsEnabled())
			funxion();
			
		this.enableFunctions.Add(funxion);
	}
	
	function AddFailFunction(funxion : Function) {
		if(this.HasFailed())
			funxion();
		
		this.enableFunctions.Add(funxion);
	}
	
	function AddSucceedFunction(funxion : Function) {
		if(this.HasSucceeded())
			funxion();
			
		this.succeedFunctions.Add(funxion);
	}
	
	/*
	 * Enables the Story Point. If the Story Point was marked as succeeded,
	 * we automatically succeed the point.
	 */
	function Enable() {
		if(this.state == State.ENABLED)
			return;
		var previousState : State = this.state;
		
		this.state = State.ENABLED;
			
		for(var funxion : Function in this.enableFunctions) {
			funxion();
		}
	
		if(previousState == State.PRESUCCEEDED)			
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
			
			for(var funxion : Function in this.succeedFunctions) {
				funxion();
			}
		
			for(child in this.children) {
				if(child.state == State.ENABLED || child.state == State.SUCCEEDED)
					continue; 
			
				var succeededParents:int = 0;
			
				for(parent in child.parents) {
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
	 * Returns all story points descendant from this one.
	 */
	function GetDescendants():List.<StoryPoint> {
		var descendants:List.<StoryPoint> = new List.<StoryPoint>(); 
		
		for(child in this.children) {
			descendants.Add(child);
			for(descendant in child.GetDescendants()) {
				descendants.Add(descendant);
			}
		}
		
		return descendants;
	}
	
	/*
	 * Enables the story point and disables all of its descendants.
	 */
	function ContinueAt() {
		this.DisableDescendants();
		
		this.Enable();
	}
	
	/**
	 * Disables all descendants of the story point.
	 */
	function DisableDescendants() {
		for(child in this.children) {
			child.state = State.DISABLED;
			
			child.DisableDescendants();
		}
	}
	
	/**
	 * Sets the Story Point's state to failed and fails all of its children.
	 * This will automatically fail all descendants of the first Story Point
	 * that is failed.
	 */
	function Fail() {
		this.state = State.FAILED;
		
		for(var funxion : Function in this.failFunctions) {
			funxion();
		}
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
	
	/*
     * Add a child to a Story Point, automatically adding the Story Point to
     * the child's parents.
     */
	function AddChild(child:StoryPoint) {
		this.children.Add(child);
		child.parents.Add(this);
	}
}
