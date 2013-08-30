package scriptease.controller.observer;

import scriptease.model.semodel.dialogue.DialogueLine;
import scriptease.model.semodel.librarymodel.LibraryModel;

/**
 * Observers are notified of changes to the story model.
 * 
 * @author kschenk
 * 
 */
public interface StoryModelObserver {

	/**
	 * Fired when a library is added to the story model.
	 * 
	 * @param library
	 */
	public void libraryAdded(LibraryModel library);

	public void dialogueRootAdded(DialogueLine root);

	public void dialogueRootRemoved(DialogueLine removed);

	public void dialogueChildAdded(DialogueLine added, DialogueLine parent);

	public void dialogueChildRemoved(DialogueLine removed, DialogueLine parent);
}
