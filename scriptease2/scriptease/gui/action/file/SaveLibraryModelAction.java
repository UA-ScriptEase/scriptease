package scriptease.gui.action.file;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.controller.io.FileIO;
import scriptease.gui.action.ActiveTranslatorSensitiveAction;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.translator.APIDictionary;
import scriptease.translator.Translator;
import scriptease.translator.Translator.DescriptionKeys;
import scriptease.translator.TranslatorManager;

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
public final class SaveLibraryModelAction extends ActiveTranslatorSensitiveAction {
	private static final String SAVE = Il8nResources.getString("Save");

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

		//TODO Removed hotkeys until we have a manager.
		
		/*this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
		this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
				KeyEvent.VK_S, ActionEvent.CTRL_MASK));*/
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
		final Translator active;
		final APIDictionary apiDictionary;
		final File location;
		
		active = TranslatorManager.getInstance().getActiveTranslator();
		apiDictionary = active.getApiDictionary();
		location = active.getPathProperty(DescriptionKeys.API_DICTIONARY_PATH
				.toString());
		
		FileIO.getInstance().writeAPIDictionary(apiDictionary, location);
	}
}
