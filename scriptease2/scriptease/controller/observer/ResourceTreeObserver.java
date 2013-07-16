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
	 * Called when the resource tree is filled with resources.
	 */
	public void resourceTreeFilled();

	/**
	 * Called when a resource is selected in a resource tree.
	 * 
	 * @param resource
	 */
	public void resourceSelected(Resource resource);

	/**
	 * Called when a resource's edit button is pressed
	 * 
	 * @param resource
	 */
	public void resourceEditButtonClicked(Resource resource);

	/**
	 * Called when the add resource button is pressed.
	 */
	public void resourceAddButtonClicked(String type);

	/**
	 * Called when a resource's edit button is pressed
	 * 
	 * @param resource
	 */
	public void resourceRemoveButtonClicked(Resource resource);

}
