package scriptease.gui.action.libraryeditor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.ScriptEase;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.pane.LibraryPanel;
import scriptease.model.CodeBlock;
import scriptease.model.CodeBlockSource;
import scriptease.model.complex.ScriptIt;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.librarymodel.LibraryModel;

/**
 * Inserts a new ScriptIt into the library. The new ScriptIt contains one empty
 * CodeBlock, which makes the library automatically identify it as an effect.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class NewEffectAction extends ActiveModelSensitiveAction {
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
	protected boolean isLegal() {
		return super.isLegal()
				&& SEModelManager.getInstance().getActiveModel() instanceof LibraryModel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final LibraryModel library;

		final ScriptIt newEffect;
		final CodeBlock codeBlock;

		library = (LibraryModel) SEModelManager.getInstance().getActiveModel();

		if (!library.getReadOnly() || ScriptEase.DEBUG_MODE) {

			newEffect = new ScriptIt(library, library.getNextID(), "New Effect");
			codeBlock = new CodeBlockSource(library, library.getNextID());

			newEffect.addCodeBlock(codeBlock);
			newEffect.setVisible(true);

			library.add(newEffect);
			LibraryPanel.getInstance().navigateToComponent(newEffect);

		}
	}
}
