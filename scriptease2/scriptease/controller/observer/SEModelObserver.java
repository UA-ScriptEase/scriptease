package scriptease.controller.observer;

import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;

/**
 * Observer interface for the {@link SEModelManager}.
 * <code>PatternModelPoolObserver</code>s are notified whenever that pool changes.
 * 
 * @author remiller
 */
public interface SEModelObserver {
	/**
	 * Notifies the receiver that the model pool has changed.
	 * 
	 * @param event
	 *            The {@link SEModel} that was added.
	 */
	public void modelChanged(SEModelEvent event);
}
