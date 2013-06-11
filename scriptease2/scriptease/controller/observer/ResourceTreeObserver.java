package scriptease.controller.observer;

import scriptease.translator.io.model.Resource;

/**
 * {@link ResourceTreeObserver}s are notified of changes to a resource tree.
 * 
 * @author kschenk
 * 
 */
public interface ResourceTreeObserver {

	/**
	 * Called when a resource is selected in a resource tree.
	 * 
	 * @param resource
	 */
	public void resourceSelected(Resource resource);
}
