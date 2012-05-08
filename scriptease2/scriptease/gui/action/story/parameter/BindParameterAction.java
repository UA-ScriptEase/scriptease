/**
 * 
 */
package scriptease.gui.action.story.parameter;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.model.atomic.KnowIt;

/**
 * Represents and performs the Bind Parameter command, as well as encapsulates
 * its enabled and name display state. <br>
 * <br>
 * Bind Parameter entails showing a binding selection to the user and re-binding
 * this action's stored {@link KnowIt} to their selection.<br>
 * <br>
 * BindParameterAction uses a sort of mix between the Factory Method and
 * Singleton design patterns. Use {@link #deriveActionForParameter(KnowIt)} to
 * get the Action that will bind that particular parameter, which may or may not
 * have already existed. There is only one BindParameterAction instance per
 * KnowIt parameter in the system.
 * 
 * @author remiller
 */
@SuppressWarnings("serial")
public final class BindParameterAction extends ActiveModelSensitiveAction {
	// TODO: internationalize this
	private static final String PICK = "Pick...";// Il8nResources.getString("Pick___");

	private final KnowIt parameter;

	// this may need to change to a map if the sizes get too large. - remiller
	private static List<BindParameterAction> actionInstanceList = new ArrayList<BindParameterAction>();

	/**
	 * Retrieves the action for the given parameter KnowIt. There should only
	 * ever be one action to bind that parameter. This is sort of a mix between
	 * the Factory Method pattern and the Singleton pattern. Each Bind action is
	 * a singleton for every parameter, but there exist multiple Bind actions in
	 * the system at one time.
	 * 
	 * @param parameter
	 * @return the action that affects the given parameter.
	 */
	public static Action deriveActionForParameter(KnowIt parameter) {
		for (BindParameterAction existingAction : BindParameterAction.actionInstanceList) {
			if (existingAction.parameter == parameter) {
				return existingAction;
			}
		}

		BindParameterAction newBindAction = new BindParameterAction(parameter);

		BindParameterAction.actionInstanceList.add(newBindAction);

		return newBindAction;
	}

	/**
	 * Defines an <code>BindParameterAction</code> object with a mnemonic.
	 * 
	 * @param parameter
	 *            The parameter that is set upon executing this action.
	 */
	private BindParameterAction(KnowIt parameter) {
		super(BindParameterAction.PICK);

		this.parameter = parameter;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// GameObject chosen =
		// WindowManager.getInstance().showPicker(parameter);
		//
		// if (chosen != null)
		// this.parameter.setBinding(chosen);
	}
}
