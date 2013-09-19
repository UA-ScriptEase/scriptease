#pragma strict

/**
 * This class defines an SEPath. It has some shortcuts that make the ScriptEase
 * II implementation much easier and cleaner. 
 *
 * The SEPath is part of the optional A* Pathfinding Library, which requires
 * Aron Granberg's A* Pathfinding Project package to be installed to your Unity
 * project. Our library was built and functions with the free version.
 *
 *@author ScriptEase II Team
 */

/**
 * We extend the AIPath class so that we can add our custom function to it,
 * which is set in a Cause.
 *
 */
class SEPath extends AIPath {
	
	var onReached:Function;
	
	/**
	 * Sets what happens when the target is reached
	 */
	function setOnTargetReached(onReached:Function) {
		this.onReached = onReached;
	}

	/**
	 * Overidden function from AIPath, since it by default does not have any
	 * functionality. All this does is call the onReached function variable.
	 */
	function OnTargetReached () {
		if(this.onReached != null)
			this.onReached();	
	}
}