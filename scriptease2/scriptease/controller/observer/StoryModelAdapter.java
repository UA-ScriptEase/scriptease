package scriptease.controller.observer;

import scriptease.model.complex.behaviours.Behaviour;
import scriptease.model.semodel.dialogue.DialogueLine;
import scriptease.model.semodel.librarymodel.LibraryModel;

/**
 * Adapter for StoryModelObserver
 * 
 * @author kschenk
 * @author jyuen
 */
public abstract class StoryModelAdapter implements StoryModelObserver {

	@Override
	public void dialogueRootAdded(DialogueLine root) {
	}

	@Override
	public void dialogueRootRemoved(DialogueLine removed) {
	}

	@Override
	public void libraryAdded(LibraryModel library) {
	}

	@Override
	public void libraryRemoved(LibraryModel library) {
	}

	@Override
	public void dialogueChildAdded(DialogueLine added, DialogueLine parent) {
	}

	@Override
	public void dialogueChildRemoved(DialogueLine removed, DialogueLine parent) {
	}

	@Override
	public void behaviourEdited(Behaviour behaviour) {
	}
}
