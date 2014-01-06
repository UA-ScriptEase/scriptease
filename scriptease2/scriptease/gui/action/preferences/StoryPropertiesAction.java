package scriptease.gui.action.preferences;

import java.awt.event.ActionEvent;

import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.action.translator.TranslatorPreferencesAction;
import scriptease.gui.dialog.DialogBuilder;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.StoryModel;

/**
 * Represents and performs the Story Properties command, as well as encapsulates its
 * enabled and name display state. <br>
 * <br>
 * Story Properties allows the user to view and edit the current story title, author and description.
 * 
 * @author zturchan
 */
@SuppressWarnings("serial")
public class StoryPropertiesAction extends ActiveModelSensitiveAction {

	private static final String STORY_PROPERTIES = "Story Properties";

	// Singleton
	private static StoryPropertiesAction instance = null;

	@Override
	protected boolean isLegal() {
		final SEModel model = SEModelManager.getInstance().getActiveModel();

		return super.isLegal() && model instanceof StoryModel;
	}

	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static StoryPropertiesAction getInstance() {
		if (instance == null) {
			instance = new StoryPropertiesAction();
		}

		return StoryPropertiesAction.instance;
	}

	/**
	 * Defines a <code>StoryPropertiesAction</code> object with a mnemonic
	 * and accelerator.
	 */
	private StoryPropertiesAction() {
		super(StoryPropertiesAction.STORY_PROPERTIES);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		DialogBuilder.getInstance().showStoryPropertiesDialog();
	}
}
