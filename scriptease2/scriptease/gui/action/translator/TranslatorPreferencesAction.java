package scriptease.gui.action.translator;

import java.awt.event.ActionEvent;

import scriptease.gui.action.ActiveTranslatorSensitiveAction;
import scriptease.gui.dialog.DialogBuilder;

@SuppressWarnings("serial")
public class TranslatorPreferencesAction extends ActiveTranslatorSensitiveAction {

	private static final String TRANSLATOR_PREFERENCES = "Translator Preferences...";

	// Singleton
	private static TranslatorPreferencesAction instance = null;

	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static TranslatorPreferencesAction getInstance() {
		if (instance == null) {
			instance = new TranslatorPreferencesAction();
		}

		return TranslatorPreferencesAction.instance;
	}

	/**
	 * Defines a <code>StoryComponentMetricsAction</code> object with a mnemonic
	 * and accelerator.
	 */
	private TranslatorPreferencesAction() {
		super(TranslatorPreferencesAction.TRANSLATOR_PREFERENCES);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		DialogBuilder.getInstance().showTranslatorPreferencesDialog();
	}
}
