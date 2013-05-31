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
 * Returns the SEPath attached to the object. Attaches an SEPath if none are
 * attached. This allows the experienced game developer to add an SEPath in the
 * editor but still make use of ScriptEase II.
 */
static function GetPath(object:GameObject):SEPath {
	var sePath:SEPath = object.GetComponent(SEPath);
		
	if(sePath == null) {
		object.AddComponent(SEPath);
		sePath = object.GetComponent(SEPath);
	}
	
	return sePath;
}

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