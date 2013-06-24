package scriptease.gui.action.file;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.controller.FileManager;
import scriptease.gui.WindowFactory;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;

/**
 * Represents and performs the Save Model Package (Save As Package)<br>
 * <br>
 * Save Model Package will save the .ses file, the module, and anything else
 * required by the game as a .zip file.
 * 
 * @author jyuen
 */
@SuppressWarnings("serial")
public class SaveModelPackageAction extends ActiveModelSensitiveAction {

	private static final String SAVE_AS_PACKAGE = "Save As Package";

	private static final Action instance = new SaveModelPackageAction();

	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @SaveModelExplicitlyActionis particular type of Action
	 */
	public static Action getInstance() {
		return instance;
	}

	/**
	 * Defines an <code>SaveModelPackageAction</code> object with a mnemonic.
	 */
	private SaveModelPackageAction() {
		super(SaveModelPackageAction.SAVE_AS_PACKAGE);

		this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
		this.putValue(
				Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK
						+ ActionEvent.SHIFT_MASK));
	}

	@Override
	protected boolean isLegal() {
		return super.isLegal();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final SEModel activeModel = SEModelManager.getInstance()
				.getActiveModel();

		if (activeModel == null)
			return;

		WindowFactory.getInstance().getCurrentFrame().requestFocusInWindow();

		FileManager.getInstance().saveAsPackage(activeModel);
	}
}
