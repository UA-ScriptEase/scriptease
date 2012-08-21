package scriptease.gui.action.file;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.controller.FileManager;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.model.PatternModel;
import scriptease.model.PatternModelManager;
import scriptease.model.StoryModel;

/**
 * Represents and performs the Save Model Explicitly (Save As...) command, as
 * well as encapsulates its enabled and name display state. <br>
 * <br>
 * Save Model Explicitly entails calling the FileManager's method for saving a
 * particular model with the currently selected model and new file destination
 * as argument.
 * 
 * @author remiller
 */
@SuppressWarnings("serial")
public class SaveStoryModelExplicitlyAction extends ActiveModelSensitiveAction {
	private static final String SAVE_AS = Il8nResources
			.getString("Save_Model_As");

	/*
	 * TODO This class needs to update legality based on what kind of model is
	 * selected. It shouldn't be legal to perform a save as with a LibraryModel,
	 * but it should with a story model.
	 */
	private static final Action instance = new SaveStoryModelExplicitlyAction();

	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @SaveModelExplicitlyActionis particular type of Action
	 */
	public static Action getInstance() {
		return instance;
	}

	/**
	 * Defines an <code>CloseModelAction</code> object with a mnemonic.
	 */
	private SaveStoryModelExplicitlyAction() {
		super(SaveStoryModelExplicitlyAction.SAVE_AS + "...");

		this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
		this.putValue(
				Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK
						+ ActionEvent.SHIFT_MASK));
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		final PatternModel activeModel = PatternModelManager.getInstance()
				.getActiveModel();

		if (activeModel == null)
			return;
		else if (activeModel instanceof StoryModel)
			FileManager.getInstance().saveAs((StoryModel) activeModel);
	}
}
