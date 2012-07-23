package scriptease.gui.action.file;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.gui.SEFrame;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.gui.pane.StoryPanel;
import scriptease.model.StoryModelPool;

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
public final class CloseStoryModelAction extends ActiveModelSensitiveAction {
	private static final String CLOSE = Il8nResources.getString("Close_Model");

	private static final Action instance = new CloseStoryModelAction();

	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static Action getInstance() {
		return CloseStoryModelAction.instance;
	}

	/**
	 * Defines a <code>SaveModelAction</code> object with a mnemonic and
	 * accelerator.
	 */
	private CloseStoryModelAction() {
		super(CloseStoryModelAction.CLOSE);

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

		if (StoryModelPool.getInstance().getActiveModel() != null) {
			StoryPanel panel = SEFrame.getInstance().getActiveStory();
			SEFrame.getInstance().removeStoryPanelTab(panel);
		}
	}
}
