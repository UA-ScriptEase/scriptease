package scriptease.controller.observer;

import scriptease.model.LibraryModel;

/**
 * Observers are notified of changes to the story model.
 * 
 * @author kschenk
 * 
 */
public interface StoryModelObserver {
	
	/**
	 * Fired when a library is added to the story model.
	 * @param library
	 */
	public void libraryAdded(LibraryModel library);

}
