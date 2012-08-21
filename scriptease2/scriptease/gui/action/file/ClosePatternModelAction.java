package scriptease.gui.action.file;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.gui.SEFrame;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.model.PatternModel;
import scriptease.model.PatternModelManager;

/**
 * Represents and performs the Close Model command, as well as encapsulates its
 * enabled and name display state. <br>
 * <br>
 * Close Model entails calling the FileManager's close method for closing a
 * particular model with the currently selected model as argument.
 * 
 * @author remiller
 */
@SuppressWarnings("serial")
public final class ClosePatternModelAction extends ActiveModelSensitiveAction {
	private static final String CLOSE = Il8nResources.getString("Close_Model");

	private static final Action instance = new ClosePatternModelAction();

	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static Action getInstance() {
		return ClosePatternModelAction.instance;
	}

	/**
	 * Defines a <code>SaveModelAction</code> object with a mnemonic and
	 * accelerator.
	 */
	private ClosePatternModelAction() {
		super(ClosePatternModelAction.CLOSE);

		this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_W);
		this.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
	}

	@Override
	protected boolean isLegal() {
		return super.isLegal()
		// removed until we actually implement undoable commands - remiller
		/*
		 * && !UndoManager.getInstance().isSaved(
		 * StoryModelPool.getInstance().getActiveModel())
		 */;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final PatternModel activeModel;

		activeModel = PatternModelManager.getInstance().getActiveModel();

		if (activeModel != null) {
			SEFrame.getInstance().removeAllComponentsForModel(activeModel);
		}
	}
}
