package scriptease.controller.observer;

import scriptease.model.complex.behaviours.Behaviour;
import scriptease.model.semodel.dialogue.DialogueLine;
import scriptease.model.semodel.librarymodel.LibraryModel;

/**
 * Observers are notified of changes to the story model.
 * 
 * @author kschenk
 * @author jyuen
 * 
 */
public interface StoryModelObserver {

	/**
	 * Fired when a library is added to the story model.
	 * 
	 * @param library
	 */
	public void libraryAdded(LibraryModel library);

	/**
	 * Fired when a library is removed from the story model.
	 * 
	 * @param library
	 */
	public void libraryRemoved(LibraryModel library);

	/**
	 * Fired when a dialogue root is added to the story model.
	 * 
	 * @param library
	 */
	public void dialogueRootAdded(DialogueLine root);

	/**
	 * Fired when a dialogue root is removed from the story model.
	 * 
	 * @param library
	 */
	public void dialogueRootRemoved(DialogueLine removed);

	/**
	 * Fired when a dialogue child is added to the story model.
	 * 
	 * @param library
	 */
	public void dialogueChildAdded(DialogueLine added, DialogueLine parent);

	/**
	 * Fired when a dialogue child is removed from the story model.
	 * 
	 * @param library
	 */
	public void dialogueChildRemoved(DialogueLine removed, DialogueLine parent);

	/**
	 * Fired when the edit button is clicked on a behaviour.
	 * 
	 * @param behaviour
	 */
	public void behaviourEditPressed(Behaviour behaviour);
}
