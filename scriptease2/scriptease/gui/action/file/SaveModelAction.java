package scriptease.gui.action.file;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.controller.FileManager;
import scriptease.gui.WindowFactory;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.model.PatternModel;
import scriptease.model.PatternModelManager;

/**
 * Represents and performs the Save Model command, as well as encapsulates its
 * enabled and name display state. <br>
 * <br>
 * Save Model entails calling the FileManager's method for saving a particular
 * model with the currently selected model as argument.
 * 
 * @author remiller
 */
@SuppressWarnings("serial")
public final class SaveModelAction extends ActiveModelSensitiveAction {
	private static final String SAVE = Il8nResources.getString("Save_Model");

	private static final Action instance = new SaveModelAction();

	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static Action getInstance() {
		return SaveModelAction.instance;
	}

	/**
	 * Defines a <code>SaveModelAction</code> object with a mnemonic and
	 * accelerator.
	 */
	private SaveModelAction() {
		super(SaveModelAction.SAVE);

		this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
		this.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
	}

	@Override
	protected boolean isLegal() {
		return super.isLegal();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final PatternModel activeModel = PatternModelManager.getInstance()
				.getActiveModel();

		if (activeModel == null)
			return;

		WindowFactory.getInstance().getCurrentFrame().requestFocusInWindow();
		
		FileManager.getInstance().save(activeModel);
	}
}
