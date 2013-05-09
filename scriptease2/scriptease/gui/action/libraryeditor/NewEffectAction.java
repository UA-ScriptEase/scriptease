package scriptease.gui.action.libraryeditor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.gui.action.ActiveTranslatorSensitiveAction;
import scriptease.model.CodeBlock;
import scriptease.model.CodeBlockSource;
import scriptease.model.LibraryModel;
import scriptease.model.SEModelManager;
import scriptease.model.complex.ScriptIt;

/**
 * Inserts a new ScriptIt into the library. The new ScriptIt contains one empty
 * CodeBlock, which makes the library automatically identify it as an effect.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class NewEffectAction extends ActiveTranslatorSensitiveAction {
	private static final String NEW_EFFECT_NAME = "Effect";

	private static final NewEffectAction instance = new NewEffectAction();

	public static NewEffectAction getInstance() {
		return instance;
	}

	protected NewEffectAction() {
		super(NEW_EFFECT_NAME);
		this.putValue(SHORT_DESCRIPTION, NEW_EFFECT_NAME);
		this.putValue(
				Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK
						+ ActionEvent.SHIFT_MASK));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final LibraryModel libraryModel;

		final ScriptIt newCause;
		final int codeBlockID;
		final CodeBlock codeBlock;

		libraryModel = (LibraryModel) SEModelManager.getInstance()
				.getActiveModel();

		newCause = new ScriptIt("Do Something");
		codeBlockID = libraryModel.getNextCodeBlockID();
		codeBlock = new CodeBlockSource(codeBlockID);

		newCause.addCodeBlock(codeBlock);
		newCause.setDisplayText("Do Something");
		newCause.setVisible(true);

		libraryModel.add(newCause);
	}
}
