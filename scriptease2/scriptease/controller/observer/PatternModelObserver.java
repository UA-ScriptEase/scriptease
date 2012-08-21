package scriptease.controller.observer;

import scriptease.model.PatternModel;
import scriptease.model.PatternModelManager;

/**
 * Observer interface for the {@link PatternModelManager}.
 * <code>PatternModelPoolObserver</code>s are notified whenever that pool changes.
 * 
 * @author remiller
 */
public interface PatternModelObserver {
	/**
	 * Notifies the receiver that the model pool has changed.
	 * 
	 * @param event
	 *            The {@link PatternModel} that was added.
	 */
	public void modelChanged(PatternModelPoolEvent event);
}
