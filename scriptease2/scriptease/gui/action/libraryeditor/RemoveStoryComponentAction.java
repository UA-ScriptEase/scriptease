package scriptease.gui.action.libraryeditor;

import java.awt.event.ActionEvent;

import scriptease.controller.VisibilityManager;
import scriptease.gui.action.ActiveTranslatorSensitiveAction;
import scriptease.model.LibraryModel;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;

/**
 * Inserts a new ScriptIt into the library.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class RemoveStoryComponentAction extends ActiveTranslatorSensitiveAction {
	private static final String REMOVE_STORY_COMPONENT_NAME = "Delete Story Component";

	private static final RemoveStoryComponentAction instance = new RemoveStoryComponentAction();

	public static RemoveStoryComponentAction getInstance() {
		return instance;
	}

	protected RemoveStoryComponentAction() {
		super(REMOVE_STORY_COMPONENT_NAME);
		this.putValue(SHORT_DESCRIPTION, REMOVE_STORY_COMPONENT_NAME);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	/*	Translator activeTranslator = TranslatorManager.getInstance()
				.getActiveTranslator();

		LibraryModel libraryModel = activeTranslator.getApiDictionary()
				.getLibrary();

		ScriptIt newCause = new ScriptIt("");
		
		// Set the visibility
		VisibilityManager.getInstance().setVisibility(newCause,
				true);
		
		libraryModel.getCausesCategory().addStoryChild(newCause);*/
	}
}
