package scriptease.gui.action.behavioureditor;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import scriptease.gui.WindowFactory;
import scriptease.translator.Translator;

/**
 * Builds the behaviour editor.
 * 
 * @author jyuen
 * 
 */
@SuppressWarnings("serial")
public class OpenBehaviourEditorAction extends AbstractAction {
	private final Translator translator;

	/**
	 * Creates an open editor action for the translator's behaviour library.
	 * 
	 * @param translator
	 */
	public OpenBehaviourEditorAction(Translator translator) {
		super("Edit " + translator.getName() + " Behaviours");
		this.translator = translator;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		WindowFactory.getInstance().buildBehaviourEditor(this.translator)
				.setVisible(true);
	}
}
