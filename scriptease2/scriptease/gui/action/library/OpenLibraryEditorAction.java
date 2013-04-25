package scriptease.gui.action.library;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import scriptease.gui.internationalization.Il8nResources;
import scriptease.model.SEModelManager;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;

@SuppressWarnings("serial")
public class OpenLibraryEditorAction extends AbstractAction {
	private static final String OPEN_MODEL = Il8nResources
			.getString("Open_Model");

	private final Translator translator;

	/**
	 * Defines an <code>OpenModelAction</code> object with accelerator and
	 * mnemonic.
	 */
	public OpenLibraryEditorAction(Translator translator) {
		super(OpenLibraryEditorAction.OPEN_MODEL + "...");

		this.translator = translator;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		TranslatorManager.getInstance().setActiveTranslator(this.translator);
		SEModelManager.getInstance().add(this.translator.getLibrary());
	}
}
