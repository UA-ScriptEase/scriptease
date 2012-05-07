package scriptease.controller.observer;

import scriptease.controller.VisibilityManager;

/**
 * Observer interface for the {@link VisibilityManager}.
 * <code>VisibilityManagerObserver</code>s are notified whenever that pool
 * changes.
 * 
 * @author mfchurch
 */
public interface VisibilityManagerObserver {
	/**
	 * Notifies the receiver that the model pool has changed.
	 * 
	 * @param event
	 *            The {@link VisibilityManager} that was modified.
	 */
	public void visibilityChanged(VisibilityManagerEvent event);
}
