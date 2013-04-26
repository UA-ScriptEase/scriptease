package scriptease.gui.action.file;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.controller.FileManager;
import scriptease.gui.WindowFactory;
import scriptease.gui.internationalization.Il8nResources;

/**
 * Represents and performs the Open Model command, as well as encapsulates its
 * enabled and name display state. <br>
 * <br>
 * Open Module entails calling the FileManager's method for opening a module
 * after allowing the user to select the module to open.
 * 
 * @author remiller
 */
@SuppressWarnings("serial")
public final class OpenStoryModelAction extends AbstractAction {
	private static final String OPEN_MODEL = Il8nResources
			.getString("Open_Model");

	private static final Action instance = new OpenStoryModelAction();

	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static Action getInstance() {
		return OpenStoryModelAction.instance;
	}

	/**
	 * Defines an <code>OpenModelAction</code> object with accelerator and
	 * mnemonic.
	 */
	private OpenStoryModelAction() {
		super(OpenStoryModelAction.OPEN_MODEL + "...");

		this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
		this.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final File location;
		
		location = WindowFactory.getInstance().showFileChooser(OPEN_MODEL, "",
				FileManager.STORY_FILTER);

		if (location != null) {
			FileManager.getInstance().openStoryModel(location);
		}
	}
}
