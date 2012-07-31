package scriptease.gui.action;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import scriptease.controller.observer.TranslatorObserver;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;

/**
 * Abstract Action implementation that defines a group of Actions whose enabled
 * state is sensitive to whether there is an active translator or not. <br>
 * <br>
 * Actions that want to be sensitive to the active translator should extend this
 * class. <br>
 * <br>
 * All or almost all of the actions that affect Story Component building
 * should have this behaviour. If a subclass wants to redefine or extend the
 * definition of when the action should be enabled, it should override
 * {@link #isLegal()}.
 * 
 * @author kschenk
 */
@SuppressWarnings("serial")
public abstract class ActiveTranslatorSensitiveAction extends AbstractAction
		implements TranslatorObserver {
	/**
	 * Builds an action that is sensitive to the model pool's active model
	 * state. It is protected to disallow non-actions from instantiating this
	 * class.
	 * 
	 * @param name
	 *            The desired name of the action.
	 */
	protected ActiveTranslatorSensitiveAction(String name) {
		super(name);

		this.updateEnabledState();
		TranslatorManager.getInstance().addTranslatorObserver(this);
	}

	@Override
	public void translatorLoaded(Translator newTranslator) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ActiveTranslatorSensitiveAction.this.updateEnabledState();
			}
		});
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
		return TranslatorManager.getInstance().getActiveTranslator() != null;
	}
}
