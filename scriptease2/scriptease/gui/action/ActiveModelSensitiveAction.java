package scriptease.gui.action;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import scriptease.controller.observer.StoryModelPoolEvent;
import scriptease.controller.observer.StoryModelPoolObserver;
import scriptease.model.StoryModelPool;

/**
 * Abstract Action implementation that defines a group of Actions whose enabled
 * state is sensitive to whether there is an active model or not. <br>
 * <br>
 * Actions that want to be sensitive to the active model should extend this
 * class. <br>
 * <br>
 * All or almost all of the actions that affect StoryComponents or StoryModels
 * should have this behaviour. If a subclass wants to redefine or extend the
 * definition of when the action should be enabled, it should override
 * {@link #isLegal()}.
 * 
 * @author remiller
 */
@SuppressWarnings("serial")
public abstract class ActiveModelSensitiveAction extends AbstractAction
		implements StoryModelPoolObserver {
	/**
	 * Builds an action that is sensitive to the model pool's active model
	 * state. It is protected to disallow non-actions from instantiating this
	 * class.
	 * 
	 * @param name
	 *            The desired name of the action.
	 */
	protected ActiveModelSensitiveAction(String name) {
		super(name);

		this.updateEnabledState();
		StoryModelPool.getInstance().addPoolChangeObserver(this);
	}

	@Override
	public void modelChanged(final StoryModelPoolEvent event) {
		if (event.getEventType() == StoryModelPoolEvent.STORY_MODEL_ACTIVATED
				|| event.getEventType() == StoryModelPoolEvent.STORY_MODEL_REMOVED) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					ActiveModelSensitiveAction.this.updateEnabledState();
				}
			});
		}
	}

	/**
	 * Updates the action to either be enabled or disabled depending on the
	 * definition of {@link #isLegal()}.
	 */
	protected final void updateEnabledState() {
		setEnabled(this.isLegal());
	}

	/**
	 * Determines if this action is a legal action to perform at the current
	 * time. This information is used to determine if it should be enabled
	 * and/or visible.<br>
	 * <br>
	 * Subclasses can override this method to extend the definition of whether
	 * the action is legal or not. If they do override it, it is <i>highly</i>
	 * recommended to include a call to <code>isLegal</code> in the returned
	 * boolean so that the whatever checks are made here also apply.
	 * 
	 * @return True if this action is legal.
	 */
	protected boolean isLegal() {
		return StoryModelPool.getInstance().hasActiveModel();
	}
}
