package scriptease.gui.action.library;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import scriptease.model.LibraryModel;
import scriptease.model.SEModelManager;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;

/**
 * Opens the editor for a Library.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class OpenLibraryEditorAction extends AbstractAction {
	private final Translator translator;

	/**
	 * Creates an open editor action for the translator's library.
	 * 
	 * @param translator
	 */
	public OpenLibraryEditorAction(Translator translator) {
		super(translator.getName() + " Default Library");
		this.translator = translator;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		TranslatorManager.getInstance().setActiveTranslator(this.translator);
		SEModelManager.getInstance().add(this.translator.getLibrary());
	}

	@Override
	public boolean isEnabled() {
		boolean isEnabled = super.isEnabled();
		if (this.translator.defaultLibraryIsLoaded()) {
			final LibraryModel library = this.translator.getLibrary();
			isEnabled &= !SEModelManager.getInstance().hasModel(library);
		}
		return isEnabled;
	}
}
