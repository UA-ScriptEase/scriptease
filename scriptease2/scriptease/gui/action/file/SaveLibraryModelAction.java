package scriptease.gui.action.file;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.internationalization.Il8nResources;

/**
 * Represents and performs the Save Library Model command, as well as encapsulates its
 * enabled and name display state. <br>
 * <br>
 * Save Library Model entails calling the FileManager's method for saving a particular
 * library model with the currently selected model as argument.
 * 
 * @author kschenk
 */
@SuppressWarnings("serial")
public final class SaveLibraryModelAction extends ActiveModelSensitiveAction {
	private static final String SAVE = Il8nResources.getString("Save_Model");

	private static final Action instance = new SaveLibraryModelAction();

	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static Action getInstance() {
		return SaveLibraryModelAction.instance;
	}

	/**
	 * Defines a <code>SaveModelAction</code> object with a mnemonic and
	 * accelerator.
	 */
	private SaveLibraryModelAction() {
		super(SaveLibraryModelAction.SAVE);

		this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
		this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
				KeyEvent.VK_S, ActionEvent.CTRL_MASK));
	}

	@Override
	protected boolean isLegal() {
		return super.isLegal()
		// removed until we actually implement undoable commands - remiller
		/*  	&& !UndoManager.getInstance().isSaved(
						StoryModelPool.getInstance().getActiveModel())*/;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//final StoryModel activeModel = StoryModelPool.getInstance()
		//		.getActiveModel();
/*
		if (activeModel == null)
			return;

		FileManager.getInstance().save(activeModel);*/
	}
}
