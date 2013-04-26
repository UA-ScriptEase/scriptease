package scriptease.gui.action.library;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import scriptease.model.SEModelManager;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;

/**
 * Opens the editor for the API Dictionary.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class OpenAPIDictionaryEditorAction extends AbstractAction {
	private final Translator translator;

	/**
	 * Creates an open editor action for the translator's api dictionary.
	 * 
	 * @param translator
	 */
	public OpenAPIDictionaryEditorAction(Translator translator) {
		super(translator.getName() + " Default Library");

		this.translator = translator;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		TranslatorManager.getInstance().setActiveTranslator(this.translator);
		SEModelManager.getInstance().add(this.translator.getLibrary());
	}
}
