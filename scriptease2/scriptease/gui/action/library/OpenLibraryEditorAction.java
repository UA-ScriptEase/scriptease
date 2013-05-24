package scriptease.gui.action.library;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import scriptease.gui.WindowFactory;
import scriptease.translator.Translator;

/**
 * Opens the a dialog that allows the user to choose a Library to edit.
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
		super("Edit " + translator.getName() + " Libraries");
		this.translator = translator;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		WindowFactory.getInstance()
				.buildLibraryEditorChoiceDialog(this.translator)
				.setVisible(true);
	}
}
