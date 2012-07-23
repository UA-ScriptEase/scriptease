package scriptease.gui.action.file;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.controller.FileManager;
import scriptease.gui.WindowManager;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.util.FileOp;

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
		this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
				KeyEvent.VK_O, ActionEvent.CTRL_MASK));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		File location = null;
		boolean tryAgain = false;
		do {
			location = WindowManager.getInstance().showFileChooser(OPEN_MODEL, FileManager.STORY_FILTER);

			if (location == null) {
				return;
				// TODO: The ScriptEase extensions (".ses" and ".sel" currently)
				// should be in a centralized config file or filemanager so the
				// save logic can use them too.
			} else if (!FileOp.getExtension(location).equalsIgnoreCase("ses")
					&& !FileOp.getExtension(location).equalsIgnoreCase("sel")) {
				tryAgain = WindowManager
						.getInstance()
						.showRetryProblemDialog(
								"Opening File",
								"The file does not have a ScriptEase-readable file extension (\".ses\" or \".sel\").\nWould you like to open a different file?",
								"Yes");
			}

		} while (tryAgain);

		if (location != null) {
			FileManager.getInstance().openStoryModel(location);
		}
	}
}
