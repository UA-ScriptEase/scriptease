package scriptease.gui.action.library;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.gui.WindowFactory;
import scriptease.model.LibraryModel;

/**
 * Opens the window that creates a new {@link LibraryModel}.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class NewLibraryAction extends AbstractAction {
	private static final String NEW_LIBRARY = "New Library";

	private static final Action instance = new NewLibraryAction();

	/**
	 * Gets the sole isntance of the action.
	 * 
	 * @return
	 */
	public static Action getInstance() {
		return NewLibraryAction.instance;
	}

	private NewLibraryAction() {
		super(NEW_LIBRARY + "...");

		this.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		WindowFactory.getInstance().showNewLibraryWizardDialog();
	}
}
