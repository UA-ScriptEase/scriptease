package scriptease.controller.observer.library;

import scriptease.model.LibraryManager;
import scriptease.model.LibraryModel;

/**
 * Observer interface for the {@link LibraryManager}.
 * <code>LibraryManagerObserver</code>s are notified whenever that pool changes.
 * 
 * @author mfchurch
 */
public interface LibraryManagerObserver {	
	/**
	 * Notifies the receiver that the model pool has changed.
	 * 
	 * @param event
	 *            The {@link LibraryModel} that was modified.
	 */
	public void modelChanged(LibraryManagerEvent event);
}
