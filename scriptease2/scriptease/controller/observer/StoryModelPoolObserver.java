package scriptease.controller.observer;

import scriptease.model.StoryModel;
import scriptease.model.StoryModelPool;



/**
 * Observer interface for the {@link StoryModelPool}.
 * <code>StoryModelPoolObserver</code>s are notified whenever that pool changes.
 * 
 * @author remiller
 */
public interface StoryModelPoolObserver {
	/**
	 * Notifies the receiver that the model pool has changed.
	 * 
	 * @param event
	 *            The {@link StoryModel} that was added.
	 */
	public void modelChanged(StoryModelPoolEvent event);
}
