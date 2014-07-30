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
 * Represents and performs the Save Model Without Writing Code Action (Save
 * without Writing Code)<br>
 * <br>
 * Save Model Without Writing Code will save the .ses file, without generating
 * any code. This will speed up the saving process.
 * 
 * @author jyuen
 * 
 */
@SuppressWarnings("serial")
public class SaveModelWithoutCodeAction extends ActiveModelSensitiveAction {

	private static final String SAVE_WITHOUT_CODE = "Save Without Writing Code";

	private static final Action instance = new SaveModelWithoutCodeAction();

	/**
	 * Gets the sole instance of this particular type of Action
	 */
	public static Action getInstance() {
		return instance;
	}

	/**
	 * Defines an <code>SaveModelWithoutCodeAction</code> object with a mnemonic.
	 */
	private SaveModelWithoutCodeAction() {
		super(SaveModelWithoutCodeAction.SAVE_WITHOUT_CODE);

		this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_W);
		this.putValue(
				Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK
						+ ActionEvent.ALT_MASK));
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

		FileManager.getInstance().saveWithoutCode(activeModel);
	}
}
